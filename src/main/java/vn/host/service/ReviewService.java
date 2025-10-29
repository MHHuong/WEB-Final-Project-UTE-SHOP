package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.host.dto.review.RatingSummary;
import vn.host.entity.Review;

import java.util.List;

public interface ReviewService {
    Page<Review> findAll(Specification<Review> spec, Pageable pageable);

    RatingSummary getRatingSummaryByProductId(Long productId);

    Review findById(Long id);

    Page<Review> findByProduct_ProductId(Long productId, Pageable pageable);
}
