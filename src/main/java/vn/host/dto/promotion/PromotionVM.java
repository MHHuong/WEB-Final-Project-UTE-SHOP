package vn.host.dto.promotion;

import lombok.*;
import vn.host.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PromotionVM {
    private Long promotionId;
    private String title;
    private String description;
    private BigDecimal discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long applyCategoryId;
    private String applyCategoryName;

    public static PromotionVM of(Promotion p) {
        return PromotionVM.builder()
                .promotionId(p.getPromotionId())
                .title(p.getTitle())
                .description(p.getDescription())
                .discountPercent(p.getDiscountPercent())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .applyCategoryId(p.getApplyCategory() == null ? null : p.getApplyCategory().getCategoryId())
                .applyCategoryName(p.getApplyCategory() == null ? "All categories" : p.getApplyCategory().getName())
                .build();
    }
}
