package vn.host.dto.coupon;

import jakarta.validation.constraints.*;
import vn.host.util.sharedenum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponReq(
        @NotBlank String code,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin(value = "0.00") BigDecimal value,
        @NotNull @DecimalMin(value = "0.00") BigDecimal minOrderAmount,
        @NotNull LocalDateTime expiredAt
) {
}