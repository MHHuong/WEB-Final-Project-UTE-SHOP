package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Review;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long>, JpaSpecificationExecutor<Review> {
    Page<Review> findByProduct_ProductId(Long productId, Pageable pageable);
    Optional<Review> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
}
