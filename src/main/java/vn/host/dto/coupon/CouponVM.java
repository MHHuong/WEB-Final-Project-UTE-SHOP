package vn.host.dto.coupon;

import lombok.*;
import vn.host.entity.Coupon;
import vn.host.util.sharedenum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CouponVM {
    private Long couponId;
    private String code;
    private DiscountType discountType;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private LocalDateTime expiredAt;

    public static CouponVM of(Coupon c) {
        return CouponVM.builder()
                .couponId(c.getCouponId())
                .code(c.getCode())
                .discountType(c.getDiscountType())
                .value(c.getValue())
                .minOrderAmount(c.getMinOrderAmount())
                .expiredAt(c.getExpiredAt())
                .build();
    }
}
