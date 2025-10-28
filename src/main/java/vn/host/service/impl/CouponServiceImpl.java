package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.host.dto.common.PageResult;
import vn.host.dto.coupon.CouponVM;
import vn.host.entity.Coupon;
import vn.host.repository.CouponRepository;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.service.CouponService;
import vn.host.spec.CouponSpecs;
import vn.host.util.sharedenum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public Coupon findById(long id) {
        return couponRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Coupon> findAppCoupons(Pageable pageable) {
        return couponRepository.findByShopIsNull(pageable);
    }

    @Override
    public Page<Coupon> searchByCode(String code, Pageable pageable) {
        return couponRepository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Override
    public Coupon save(Coupon coupon) {
        validateCoupon(coupon);
        return couponRepository.save(coupon);
    }

    @Override
    public void delete(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    public void delete(long id) {
        couponRepository.deleteById(id);
    }

    private void validateCoupon(Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().isEmpty())
            throw new IllegalArgumentException("Mã coupon không được để trống!");

        if (coupon.getDiscountType() == DiscountType.PERCENT &&
                (coupon.getValue().compareTo(BigDecimal.ZERO) <= 0 ||
                        coupon.getValue().compareTo(BigDecimal.valueOf(100)) > 0))
            throw new IllegalArgumentException("Giá trị phần trăm phải từ 1 đến 100!");

        if (coupon.getDiscountType() == DiscountType.AMOUNT &&
                coupon.getValue().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0!");

        if (coupon.getExpiredAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Ngày hết hạn phải sau thời điểm hiện tại!");

        if (coupon.getMinOrderAmount() != null && coupon.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không được nhỏ hơn 0!");
        }

        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Mã coupon '" + coupon.getCode() + "' đã tồn tại!");
        }
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public Page<Coupon> findByShop_ShopId(long shopId, Pageable pageable) {
        return couponRepository.findByShop_ShopId(shopId, pageable);
    }

    @Override
    public PageResult<CouponVM> searchOwnerCoupons(String userEmail, String q, String status, int page, int size, Sort sort) {
        var user = userRepository.findByEmail(userEmail).orElseThrow(() -> new SecurityException("User not found"));
        var shop = shopRepository.findFirstByOwner_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));
        Long shopId = shop.getShopId();

        Pageable pageable = PageRequest.of(page, size, (sort == null) ? Sort.by(Sort.Direction.DESC, "expiredAt") : sort);

        List<Specification<Coupon>> list = new ArrayList<>();
        list.add(CouponSpecs.belongsToShop(shopId));
        if (StringUtils.hasText(q)) list.add(CouponSpecs.codeContains(q));

        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.hasText(status)) {
            String s = status.trim().toLowerCase();
            if (s.equals("active")) list.add(CouponSpecs.statusActive(now));
            else if (s.equals("expired")) list.add(CouponSpecs.statusExpired(now));
        }

        Specification<Coupon> spec = Specification.allOf(list);
        Page<Coupon> pg = couponRepository.findAll(spec, pageable);

        var content = pg.getContent().stream().map(CouponVM::of).toList();
        return PageResult.<CouponVM>builder()
                .content(content)
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();
    }

    @Override
    public List<Coupon> findAllGlobalCoupons() {
        return couponRepository.findGlobalCoupon();
    }

    @Override
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    @Override
    public List<Coupon> findShopCoupons(Long shopId) {
        return couponRepository.findShopCoupon(shopId);
    }
}
