package vn.host.dto.review;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewItemRes {
    private Long reviewId;

    private Long productId;
    private String productName;

    private Long userId;
    private String userName;

    private Integer rating;
    private String comment;
    private Instant createdAt;

    private List<ReviewMediaRes> media;
}