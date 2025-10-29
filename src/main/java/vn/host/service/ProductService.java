package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.host.dto.common.ProductDTO;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductDetailVM;
import vn.host.dto.product.ProductListItemVM;
import vn.host.entity.Product;
import vn.host.model.response.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Product> findAllForAdmin(String keyword, Long categoryId, Long shopId, Integer status, Pageable pageable);

    void updateStatus(Long id, Integer status);

    void deleteById(Long id);

    List<Product> findAll();

    Product findById(Long id);

    Page<Product> searchByName(String name, Pageable pageable);

    Page<Product> searchByShopName(String shopName, Pageable pageable);

    List<Product> findByShopId(long shopId);

    List<Product> findByCategoryId(long id);

    Page<ProductDTO> findAllProducts(Pageable pageable);

    Page<ProductDTO> findProductsByCategory(String categoryName, Pageable pageable);

    List<ProductDTO> findAllProductsAsDTO();

    Page<ProductDTO> findProductsByCategoryId(Long categoryId, Pageable pageable);

    Page<ProductDTO> findAllProductsFiltered(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<ProductDTO> findProductsByCategoryIdFiltered(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    void save(Product product);

    void delete(long id);

    Product findById(long id);

    PageResult<ProductListItemVM> searchOwnerProducts(String userEmail, String q, Long categoryId, Integer status, BigDecimal minPrice, BigDecimal maxPrice, int page, int size, Sort sort);

    void softDeleteOwnerProduct(String userEmail, long productId);

    void restoreOwnerProduct(String userEmail, long productId, int toStatus);

    void bulkUpdateStatus(String userEmail, List<Long> productIds, int status);

    List<ProductResponse> findAllProductOrder();

    ProductDetailVM getProductDetailVM(Long productId);
}