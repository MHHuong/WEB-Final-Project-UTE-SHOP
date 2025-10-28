package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByShop_ShopId(Long shopId, Pageable pageable);
    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pageable);
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
}