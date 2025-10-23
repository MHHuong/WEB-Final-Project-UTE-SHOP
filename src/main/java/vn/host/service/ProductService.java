package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Product;

public interface ProductService {
    Page<Product> findAllForAdmin(String keyword, Long categoryId, Long shopId, Integer status, Pageable pageable);
    Product findById(Long id);
    Product save(Product product);
    void updateStatus(Long id, Integer status);
    void deleteById(Long id);
    Page<Product> searchByName(String name, Pageable pageable);
    Page<Product> searchByShopName(String shopName, Pageable pageable);
}
