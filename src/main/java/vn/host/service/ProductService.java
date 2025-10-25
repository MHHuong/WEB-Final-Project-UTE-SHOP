package vn.host.service;

import org.springframework.data.domain.Sort;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductListItemVM;
import vn.host.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    void save(Product product);

    void delete(long id);

    List<Product> findAll();

    Product findById(long id);

    List<Product> findByShopId(long shopId);

    List<Product> findByCategoryId(long id);

    PageResult<ProductListItemVM> searchOwnerProducts(String userEmail, String q, Long categoryId, Integer status, BigDecimal minPrice, BigDecimal maxPrice, int page, int size, Sort sort);

    void softDeleteOwnerProduct(String userEmail, long productId);

    void restoreOwnerProduct(String userEmail, long productId, int toStatus);
}
