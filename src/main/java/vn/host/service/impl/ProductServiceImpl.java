package vn.host.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.dto.ProductDTO;
import vn.host.entity.Product;
import vn.host.repository.ProductRepository;
import vn.host.service.CategoryService;
import vn.host.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        productRepository.deleteById(id);
    }
    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = findById(id);
        product.setStatus(status);
        productRepository.save(product);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAllWithDetails();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByShopId(long shopId) {
        return productRepository.findByShopIdWithDetails(shopId);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategoryId(long id) {
        return productRepository.findByCategoryIdWithDetails(id);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findProductsByCategory(String categoryName, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryName(categoryName, pageable);
        return productPage.map(this::convertToDTO);
    }
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getMedia() != null && !product.getMedia().isEmpty()) {
            dto.setImageUrl(product.getMedia().iterator().next().getUrl());
        } else {
            dto.setImageUrl("/assets/images/products/product-img-default.jpg"); // Ảnh mặc định
        }
        double avgRating = 0.0;
        int reviewCount = 0;
        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            avgRating = product.getReviews().stream()
                    .mapToDouble(review -> review.getRating())
                    .average()
                    .orElse(0.0);
            reviewCount = product.getReviews().size();
        }

        dto.setAverageRating(avgRating);
        dto.setReviewCount(reviewCount);

        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> findAllProductsAsDTO() {
        List<Product> products = this.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findProductsByCategoryId(Long categoryId, Pageable pageable) {
        Set<Long> allCategoryIds = categoryService.getCategoryAndDescendantIds(categoryId);
        if (allCategoryIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Product> productPage = productRepository.findByCategoryIdsIn(allCategoryIds, pageable);
        return productPage.map(this::convertToDTO);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllProductsFiltered(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> productPage = productRepository.findAllWithPriceFilter(minPrice, maxPrice, pageable);
        return productPage.map(this::convertToDTO);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findProductsByCategoryIdFiltered(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Set<Long> allCategoryIds = categoryService.getCategoryAndDescendantIds(categoryId);
        if (allCategoryIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Product> productPage = productRepository.findByCategoryIdsInAndPriceFilter(allCategoryIds, minPrice, maxPrice, pageable);
        return productPage.map(this::convertToDTO);
    }
    @Override
    public Page<Product> findAllForAdmin(String keyword, Long categoryId, Long shopId, Integer status, Pageable pageable) {
        Specification<Product> spec = Specification.allOf();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (shopId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("shop").get("shopId"), shopId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return productRepository.findAll(spec, pageable);
    }
    @Override
    public Page<Product> searchByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> searchByShopName(String shopName, Pageable pageable) {
        return productRepository.findByShopNameContainingIgnoreCase(shopName, pageable);
    }
}
