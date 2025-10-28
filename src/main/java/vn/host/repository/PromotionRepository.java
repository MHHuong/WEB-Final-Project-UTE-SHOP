package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Category;
import vn.host.entity.Promotion;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion,Long>, JpaSpecificationExecutor<Promotion> {
    Page<Promotion> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    // Promotion đang hoạt động (toàn hệ thống hoặc theo category)
    @Query("SELECT p FROM Promotion p WHERE " +
            "(:category IS NULL OR p.applyCategory = :category) " +
            "AND (p.shop IS NULL) " + // loại bỏ theo shop
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotions(Category category, LocalDate today);
}
