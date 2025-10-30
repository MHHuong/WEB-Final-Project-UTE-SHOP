package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.host.dto.review.RatingSummary;
import vn.host.dto.review.ReviewItemRes;
import vn.host.dto.review.ReviewMediaRes;
import vn.host.entity.Review;
import vn.host.entity.ReviewMedia;
import vn.host.repository.ReviewRepository;
import vn.host.service.ReviewService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepo;
    private final ReviewMediaServiceImpl reviewMediaService;

    @Override
    public Page<Review> findAll(Specification<Review> spec, Pageable pageable) {
        return reviewRepo.findAll(spec, pageable);
    }

    @Override
    public RatingSummary getRatingSummaryByProductId(Long productId) {
        return reviewRepo.getRatingSummaryByProductId(productId);
    }

    @Override
    public Review findById(Long id) {
        return reviewRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    @Override
    public Page<Review> findByProduct_ProductId(Long productId, Pageable pageable) {
        return reviewRepo.findByProduct_ProductId(productId, pageable);
    }
    @Override
    public List<ReviewItemRes> getReviewsByProductIdVM(Long productId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepo.findByProduct_ProductId(productId, pageable);
        return reviewPage.getContent().stream()
                .map(this::mapToReviewItemRes)
                .collect(Collectors.toList());
    }

    private ReviewItemRes mapToReviewItemRes(Review review) {
        List<ReviewMedia> mediaEntities = reviewMediaService.findByReview_ReviewId(review.getReviewId());
        List<ReviewMediaRes> mediaResList = mediaEntities.stream()
                .map(media -> ReviewMediaRes.builder()
                        .id(media.getReviewMediaId())
                        .url(media.getUrl())
                        .type(media.getType().name())
                        .build())
                .collect(Collectors.toList());
        return ReviewItemRes.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProduct() != null ? review.getProduct().getProductId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : null)
                .userId(review.getUser() != null ? review.getUser().getUserId() : null)
                .userName(review.getUser() != null ? review.getUser().getFullName() : null) // Giả định
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .media(mediaResList)
                .build();
    }
}