package vn.host.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Product;
import vn.host.model.response.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE p.status = 0")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE p.productId = :id AND p.status = 0")
    Product findByIdWithDetails(@Param("id") long id);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.media " +
            "LEFT JOIN FETCH p.reviews " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.shop " +
            "WHERE p.shop.shopId = :shopId AND p.status = 0")
    List<Product> findByShopIdWithDetails(@Param("shopId") long shopId);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.status = 0",
            countQuery = "SELECT count(p) FROM Product p WHERE p.status = 0")
    Page<Product> findAll(Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE c.name = :categoryName AND p.status = 0",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE c.name = :categoryName AND p.status = 0")
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE c.categoryId = :categoryId AND p.status = 0",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE c.categoryId = :categoryId AND p.status = 0")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE c.categoryId IN :categoryIds AND p.status = 0",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c WHERE c.categoryId IN :categoryIds AND p.status = 0")
    Page<Product> findByCategoryIdsIn(@Param("categoryIds") Set<Long> categoryIds, Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c " +
            "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND p.status = 0",
            countQuery = "SELECT count(p) FROM Product p " +
                    "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND p.status = 0")
    Page<Product> findAllWithPriceFilter(@Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c " +
            "WHERE c.categoryId IN :categoryIds " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND p.status = 0",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c " +
                    "WHERE c.categoryId IN :categoryIds " +
                    "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND p.status = 0")
    Page<Product> findByCategoryIdsInAndPriceFilter(@Param("categoryIds") Set<Long> categoryIds,
                                                    @Param("minPrice") BigDecimal minPrice,
                                                    @Param("maxPrice") BigDecimal maxPrice,
                                                    Pageable pageable);

    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c " +
            "WHERE c.categoryId = :categoryId " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND p.status = 0",
            countQuery = "SELECT count(p) FROM Product p LEFT JOIN p.category c " +
                    "WHERE c.categoryId = :categoryId " +
                    "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR p.price <= :maxPrice) AND p.status = 0")
    Page<Product> findByCategoryIdAndPriceFilter(@Param("categoryId") Long categoryId,
                                                 @Param("minPrice") BigDecimal minPrice,
                                                 @Param("maxPrice") BigDecimal maxPrice,
                                                 Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.shop.shopName) LIKE LOWER(CONCAT('%', :shopName, '%'))")
    Page<Product> findByShopNameContainingIgnoreCase(@Param("shopName") String shopName, Pageable pageable);

    Page<Product> findByShop_ShopId(Long shopId, Pageable pageable);

    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);

    @Query("""
                SELECT new vn.host.model.response.ProductResponse(
                    p.productId,
                    s.shopId,
                    s.shopName,
                    p.name,
                    p.price,
                    m.url
                )
                FROM Product p
                JOIN p.shop s
                JOIN p.media m
                WHERE m.type = 'IMAGE'
            """)
    List<ProductResponse> findAllProductsOrder();

    Page<Product> findByStatus(int status, Pageable pageable);
}