package vn.host.dto.common;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String categoryName;
    private String imageUrl;
    private double averageRating;
    private int reviewCount;
}