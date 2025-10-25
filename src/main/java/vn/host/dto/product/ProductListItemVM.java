package vn.host.dto.product;

import lombok.*;
import vn.host.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListItemVM {
    private Long productId;
    private String name;
    private String categoryName;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private Instant createdAt;
    private String thumbnailUrl;

    public static ProductListItemVM of(Product p, String thumb) {
        return ProductListItemVM.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .price(p.getPrice())
                .stock(p.getStock())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .thumbnailUrl(thumb)
                .build();
    }
}