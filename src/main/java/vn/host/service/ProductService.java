package vn.host.service;

import vn.host.entity.Product;

import java.util.List;

public interface ProductService {
    void save(Product product);
    void delete(long id);
    List<Product> findAll();
    Product findById(long id);
    List<Product> findByShopId(long shopId);
    List<Product> findByCategoryId(long id);
}