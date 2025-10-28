package vn.host.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.dto.ProductDTO;
import vn.host.entity.Product;
import vn.host.repository.ProductRepository; // (File này ở mục 2)
import vn.host.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void save(Product product) {
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy Product với id: " + id + " để xóa.");
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAllWithDetails();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(long id) {
        Product product = productRepository.findByIdWithDetails(id);
        if (product == null) {
            throw new EntityNotFoundException("Không tìm thấy Product với id: " + id);
        }
        return product;
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

        // Lấy tên category (kiểm tra null an toàn)
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        // Lấy ảnh đầu tiên (kiểm tra null/empty an toàn)
        if (product.getMedia() != null && !product.getMedia().isEmpty()) {
            // Giả sử bạn muốn lấy URL của ảnh đầu tiên
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
        // 1. Gọi phương thức findAll() cũ của bạn để lấy Entity
        List<Product> products = this.findAll();

        // 2. Chuyển đổi List<Product> sang List<ProductDTO>
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}