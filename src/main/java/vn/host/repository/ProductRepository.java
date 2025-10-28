package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Product;
import vn.host.model.response.ProductResponse;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
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
}