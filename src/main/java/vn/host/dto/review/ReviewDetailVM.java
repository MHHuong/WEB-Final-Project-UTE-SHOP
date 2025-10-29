package vn.host.dto.review;

import lombok.*;
import vn.host.dto.product.ProductMediaVM;
import vn.host.entity.Review;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailVM {
    private Long reviewId;
    private int rating;
    private String comment;
    private Instant createdAt;

    private Long userId;
    private String userName;

    private Long productId;
    private String productName;
    private String categoryName;

    private List<ProductMediaVM> media;

    public static ReviewDetailVM of(Review r, String categoryName,
                                    List<ProductMediaVM> media) {
        return ReviewDetailVM.builder()
                .reviewId(r.getReviewId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())

                .userId(r.getUser() != null ? r.getUser().getUserId() : null)
                .userName(r.getUser() != null ? r.getUser().getFullName() : null)

                .productId(r.getProduct() != null ? r.getProduct().getProductId() : null)
                .productName(r.getProduct() != null ? r.getProduct().getName() : null)
                .categoryName(categoryName)
                .media(media)
                .build();
    }
}