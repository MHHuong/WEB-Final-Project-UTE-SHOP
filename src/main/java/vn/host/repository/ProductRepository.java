package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByShop_ShopId(Long shopId, Pageable pageable);
    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
