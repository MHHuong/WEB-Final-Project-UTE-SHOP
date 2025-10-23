package vn.host.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.shop.shopName) LIKE LOWER(CONCAT('%', :shopName, '%'))")
    Page<Product> findByShopNameContainingIgnoreCase(@Param("shopName") String shopName, Pageable pageable);
}
