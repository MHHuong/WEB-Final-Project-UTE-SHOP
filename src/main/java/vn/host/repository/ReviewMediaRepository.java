package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.ReviewMedia;

import java.util.List;

@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia,Long>, JpaSpecificationExecutor<ReviewMedia> {
    List<ReviewMedia> findByReview_ReviewId(Long reviewId);
}
