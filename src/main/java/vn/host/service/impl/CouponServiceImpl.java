package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.Coupon;
import vn.host.repository.CouponRepository;
import vn.host.service.CouponService;
import vn.host.util.sharedenum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {


    @Autowired
    private CouponRepository couponRepository;

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
}
