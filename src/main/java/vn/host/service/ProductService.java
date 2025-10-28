package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.dto.ProductDTO;
import vn.host.entity.Product;

import java.util.List;

public interface ProductService {
    void save(Product product);
    void delete(long id);
    List<Product> findAll();
    Product findById(long id);
    List<Product> findByShopId(long shopId);
    List<Product> findByCategoryId(long id);
    Page<ProductDTO> findAllProducts(Pageable pageable);
    Page<ProductDTO> findProductsByCategory(String categoryName, Pageable pageable);
    List<ProductDTO> findAllProductsAsDTO();
    Page<ProductDTO> findProductsByCategoryId(Long categoryId, Pageable pageable);
}