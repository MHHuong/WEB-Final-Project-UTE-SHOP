package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.dto.review.RatingSummary;
import vn.host.entity.Review;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    @Query("""
            select coalesce(avg(r.rating), 0.0) as avg,
                   coalesce(count(r), 0) as total
            from Review r
            where r.product.productId = :productId
            """)
    RatingSummary getRatingSummaryByProductId(Long productId);

    Optional<Review> findById(Long id);

    Page<Review> findByProduct_ProductId(Long productId, Pageable pageable);
}