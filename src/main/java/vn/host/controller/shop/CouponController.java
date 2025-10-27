package vn.host.controller.shop;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.coupon.CouponReq;
import vn.host.dto.coupon.CouponVM;
import vn.host.entity.Coupon;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.service.CouponService;
import vn.host.service.ShopService;
import vn.host.service.UserService;
import vn.host.util.sharedenum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/shop/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final ShopService shopService;
    private final UserService userService;

    private User authedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Unauthenticated");
        return userService.findByEmail(auth.getName());
    }

    private Shop myShopOr403(User u) {
        Shop s = shopService.getMyShopOrNull(u.getUserId());
        if (s == null) throw new SecurityException("Shop not registered");
        return s;
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) return Sort.by(Sort.Direction.DESC, "expiredAt");
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    @GetMapping
    public ResponseEntity<PageResult<CouponVM>> search(
            Authentication auth,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Unauthenticated");
        return ResponseEntity.ok(
                couponService.searchOwnerCoupons(auth.getName(), q, status, page, size, parseSort(sort))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponVM> detail(Authentication auth, @PathVariable Long id) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Coupon c = couponService.findById(id);
        if (c == null || c.getShop() == null || !s.getShopId().equals(c.getShop().getShopId()))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(CouponVM.of(c));
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication auth, @Valid @RequestBody CouponReq req) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);
        validateBusinessRules(req, null);

        Optional<Coupon> existed = couponService.findByCode(req.code());
        if (existed.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Coupon code already exists.");
        }

        Coupon c = Coupon.builder()
                .code(req.code().trim())
                .discountType(req.discountType())
                .value(req.value())
                .minOrderAmount(req.minOrderAmount() == null ? BigDecimal.ZERO : req.minOrderAmount())
                .expiredAt(req.expiredAt())
                .shop(s)
                .build();

        couponService.save(c);
        return ResponseEntity.ok(CouponVM.of(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody CouponReq req) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Coupon c = couponService.findById(id);
        if (c == null || c.getShop() == null || !s.getShopId().equals(c.getShop().getShopId()))
            return ResponseEntity.notFound().build();

        validateBusinessRules(req, id);

        // nếu đổi code → check unique
        if (!c.getCode().equals(req.code().trim())) {
            if (couponService.findByCode(req.code()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Coupon code already exists.");
        }

        c.setCode(req.code().trim());
        c.setDiscountType(req.discountType());
        c.setValue(req.value());
        c.setMinOrderAmount(req.minOrderAmount() == null ? BigDecimal.ZERO : req.minOrderAmount());
        c.setExpiredAt(req.expiredAt());
        couponService.save(c);

        return ResponseEntity.ok(CouponVM.of(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Coupon c = couponService.findById(id);
        if (c == null || c.getShop() == null || !s.getShopId().equals(c.getShop().getShopId()))
            return ResponseEntity.notFound().build();

        try {
            couponService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa: Coupon đang được tham chiếu bởi dữ liệu khác (khóa ngoại).");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Xóa thất bại.");
        }
    }

    private void validateBusinessRules(CouponReq req, Long ignoreId) {
        if (req.discountType() == DiscountType.PERCENT) {
            var v = req.value();
            if (v == null || v.compareTo(BigDecimal.ONE) < 0 || v.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("For PERCENT, value must be between 1 and 100.");
            }
        } else if (req.discountType() == DiscountType.AMOUNT) {
            if (req.value() == null || req.value().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("For AMOUNT, value must be greater than 0.");
            }
        }

        if (req.expiredAt() == null || !req.expiredAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("expiredAt must be after the current time.");
        }

        if (req.minOrderAmount() != null && req.minOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("minOrderAmount must be >= 0.");
        }
    }
}
