package vn.host.dto.product;

import lombok.*;
import vn.host.entity.Product;
import vn.host.entity.ProductMedia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailVM {
    private Long productId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String description;
    private Instant createdAt;

    private List<ProductMediaVM> media;

    private double avgRating;
    private long totalReviews;

    public static ProductDetailVM of(Product p, List<ProductMedia> media,
                                     Double avg, Long total) {
        return ProductDetailVM.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .categoryId(p.getCategory() != null ? p.getCategory().getCategoryId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .shopId(p.getShop() != null ? p.getShop().getShopId() : null)
                .price(p.getPrice())
                .stock(p.getStock())
                .status(p.getStatus())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .media(media != null
                        ? media.stream().map(m -> new ProductMediaVM(m.getMediaId(), m.getUrl(), m.getType().name())).toList()
                        : List.of())
                .avgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(total != null ? total : 0L)
                .build();
    }
}