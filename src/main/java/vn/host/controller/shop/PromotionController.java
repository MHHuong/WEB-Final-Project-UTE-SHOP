package vn.host.controller.shop;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.promotion.PromotionReq;
import vn.host.dto.promotion.PromotionVM;
import vn.host.entity.*;
import vn.host.service.CategoryService;
import vn.host.service.PromotionService;
import vn.host.service.ShopService;
import vn.host.service.UserService;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shop/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;
    private final CategoryService categoryService;
    private final ShopService shopService;
    private final UserService userService;

    /**
     * Lấy User từ Authentication giống ShopController
     */
    private User authedUser(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        return userService.getUserByEmail(auth.getName());
    }

    /**
     * Lấy Shop của owner hiện tại; ném 403 nếu chưa có shop
     */
    private Shop myShopOr403(User u) {
        Shop s = shopService.getMyShopOrNull(u.getUserId());
        if (s == null) throw new SecurityException("Shop not registered");
        return s;
    }

    // LIST (phân trang) — cho trang promotions.html
    @GetMapping
    public ResponseEntity<PageResult<PromotionVM>> list(Authentication auth,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Promotion> p = promotionService.findByShop_ShopId(s.getShopId(), pageable);

        var rs = new PageResult<PromotionVM>();
        rs.setContent(p.getContent().stream().map(PromotionVM::of).collect(Collectors.toList()));
        rs.setPage(p.getNumber());
        rs.setSize(p.getSize());
        rs.setTotalElements(p.getTotalElements());
        rs.setTotalPages(p.getTotalPages());
        return ResponseEntity.ok(rs);
    }

    // DETAIL — cho trang edit-promotions.html load dữ liệu
    @GetMapping("/{id}")
    public ResponseEntity<PromotionVM> detail(Authentication auth, @PathVariable Long id) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Promotion p = promotionService.findById(id);
        if (p == null || p.getShop() == null || !p.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(PromotionVM.of(p));
    }

    // TẠO — cho add-promotions.html
    @PostMapping
    public ResponseEntity<PromotionVM> create(Authentication auth, @Valid @RequestBody PromotionReq req) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        if (req.startDate().isAfter(req.endDate())) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }

        // RÀNG BUỘC CHỒNG LẤN
        boolean overlap;
        if (req.applyCategoryId() == null) {
            // Global promo: không được chồng bất kỳ chương trình nào
            overlap = promotionService.existsOverlappingGlobal(s.getShopId(), req.startDate(), req.endDate(), null);
        } else {
            overlap = promotionService.existsOverlappingForCategory(
                    s.getShopId(), req.applyCategoryId(), req.startDate(), req.endDate(), null
            );
        }
        if (overlap) {
            throw new IllegalStateException("Promotion overlaps with an existing one in this time range");
        }

        Category cat = null;
        if (req.applyCategoryId() != null) {
            cat = categoryService.findById(req.applyCategoryId());
        }

        Promotion p = Promotion.builder()
                .shop(s)
                .title(req.title())
                .description(req.description())
                .discountPercent(req.discountPercent())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .applyCategory(cat) // null = toàn shop
                .build();

        promotionService.save(p);
        return ResponseEntity.ok(PromotionVM.of(p));
    }

    // SỬA — cho edit-promotions.html
    @PutMapping("/{id}")
    public ResponseEntity<PromotionVM> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody PromotionReq req) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Promotion p = promotionService.findById(id);
        if (p == null || p.getShop() == null || !p.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }
        if (req.startDate().isAfter(req.endDate())) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }

        boolean overlap;
        if (req.applyCategoryId() == null) {
            overlap = promotionService.existsOverlappingGlobal(s.getShopId(), req.startDate(), req.endDate(), id);
        } else {
            overlap = promotionService.existsOverlappingForCategory(
                    s.getShopId(), req.applyCategoryId(), req.startDate(), req.endDate(), id
            );
        }
        if (overlap) {
            throw new IllegalStateException("Promotion overlaps with an existing one in this time range");
        }

        Category cat = null;
        if (req.applyCategoryId() != null) {
            cat = categoryService.findById(req.applyCategoryId());
        }

        p.setTitle(req.title());
        p.setDescription(req.description());
        p.setDiscountPercent(req.discountPercent());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());
        p.setApplyCategory(cat);

        promotionService.save(p);
        return ResponseEntity.ok(PromotionVM.of(p));
    }

    // XÓA MỀM — expire (đặt endDate về hôm qua)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication auth, @PathVariable Long id) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Promotion p = promotionService.findById(id);
        if (p == null || p.getShop() == null || !p.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }

        try {
            // XÓA CỨNG
            promotionService.delete(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (DataIntegrityViolationException ex) {
            // Spring bọc lỗi FK vào đây
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Không thể xóa: Promotion đang được tham chiếu bởi dữ liệu khác (khóa ngoại).");
        } catch (PersistenceException ex) {
            // Một số JPA provider ném PersistenceException/ConstraintViolationException
            Throwable cause = ex.getCause();
            if (cause instanceof ConstraintViolationException) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Không thể xóa: Promotion đang được tham chiếu bởi dữ liệu khác (khóa ngoại).");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Xóa thất bại do lỗi hệ thống.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Xóa thất bại.");
        }
    }
}