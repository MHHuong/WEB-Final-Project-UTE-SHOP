package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop")
    List<Product> findAllWithDetails();
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE p.productId = :id")
    Product findByIdWithDetails(@Param("id") long id);
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE p.shop.shopId = :shopId")
    List<Product> findByShopIdWithDetails(@Param("shopId") long shopId);
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE p.category.categoryId = :categoryId")
    List<Product> findByCategoryIdWithDetails(@Param("categoryId") long categoryId);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.reviews",
            countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAll(Pageable pageable);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.reviews WHERE c.name = :categoryName",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE c.name = :categoryName")
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.reviews WHERE c.categoryId = :categoryId",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE c.categoryId = :categoryId")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.reviews WHERE c.categoryId IN :categoryIds",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE c.categoryId IN :categoryIds")
    Page<Product> findByCategoryIdsIn(@Param("categoryIds") Set<Long> categoryIds, Pageable pageable);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.reviews " +
            "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
            countQuery = "SELECT count(p) FROM Product p " +
                    "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findAllWithPriceFilter(@Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         Pageable pageable);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.reviews " +
            "WHERE c.categoryId IN :categoryIds " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c " +
                    "WHERE c.categoryId IN :categoryIds " +
                    "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByCategoryIdsInAndPriceFilter(@Param("categoryIds") Set<Long> categoryIds,
                                                    @Param("minPrice") BigDecimal minPrice,
                                                    @Param("maxPrice") BigDecimal maxPrice,
                                                    Pageable pageable);
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.reviews " +
            "WHERE c.categoryId = :categoryId " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c " +
                    "WHERE c.categoryId = :categoryId " +
                    "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findByCategoryIdAndPriceFilter(@Param("categoryId") Long categoryId,
                                                 @Param("minPrice") BigDecimal minPrice,
                                                 @Param("maxPrice") BigDecimal maxPrice,
                                                 Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT p FROM Product p WHERE LOWER(p.shop.shopName) LIKE LOWER(CONCAT('%', :shopName, '%'))")
    Page<Product> findByShopNameContainingIgnoreCase(@Param("shopName") String shopName, Pageable pageable);
}