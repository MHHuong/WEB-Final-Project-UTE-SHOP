package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Category;
import vn.host.entity.Promotion;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {
    Page<Promotion> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    // Promotion đang hoạt động (toàn hệ thống hoặc theo category)
    @Query("SELECT p FROM Promotion p WHERE " +
            "(:category IS NULL OR p.applyCategory = :category) " +
            "AND (p.shop IS NULL) " + // loại bỏ theo shop
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotions(Category category, LocalDate today);

    Page<Promotion> findByShop_ShopId(Long shopId, Pageable pageable);

    @Query("""
                select count(p) > 0 from Promotion p
                where p.shop.shopId = :shopId
                  and (:ignoreId is null or p.promotionId <> :ignoreId)
                  and p.startDate <= :endDate and p.endDate >= :startDate
            """)
    boolean existsOverlappingGlobal(@Param("shopId") Long shopId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("ignoreId") Long ignoreId);

    @Query("""
                select count(p) > 0 from Promotion p
                where p.shop.shopId = :shopId
                  and (:ignoreId is null or p.promotionId <> :ignoreId)
                  and p.startDate <= :endDate and p.endDate >= :startDate
                  and (
                        p.applyCategory is null
                        or p.applyCategory.categoryId = :categoryId
                      )
            """)
    boolean existsOverlappingForCategory(@Param("shopId") Long shopId,
                                         @Param("categoryId") Long categoryId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("ignoreId") Long ignoreId);

    @Query("""
                select p from Promotion p
                where p.shop.shopId = :shopId
                  and p.startDate <= :toDate
                  and p.endDate   >= :fromDate
            """)
    List<Promotion> findActiveInRangeForShop(@Param("shopId") Long shopId,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate);
}
