package vn.host.dto.promotion;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionReq(
        @NotBlank String title,
        String description,
        @NotNull @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
        BigDecimal discountPercent,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        Long applyCategoryId
) {
}