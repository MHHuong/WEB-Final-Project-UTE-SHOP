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
    @Query("select avg(r.rating) as avg, count(r) as total " +
            "from Review r where r.product.productId = :productId")
    RatingSummary getRatingSummaryByProductId(Long productId);

    Optional<Review> findById(Long id);
}
