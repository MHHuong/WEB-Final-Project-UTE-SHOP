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
public class ProductVM {
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

    public static ProductVM of(Product p, List<ProductMedia> media) {
        return ProductVM.builder()
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
                        ? media.stream().map(m ->
                        new ProductMediaVM(m.getMediaId(), m.getUrl(), m.getType().name())
                ).toList()
                        : List.of())
                .build();
    }
}