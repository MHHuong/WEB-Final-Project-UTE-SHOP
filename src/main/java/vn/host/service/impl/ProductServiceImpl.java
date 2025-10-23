package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.entity.Product;
import vn.host.repository.ProductRepository;
import vn.host.service.ProductService;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

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
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = findById(id);
        product.setStatus(status);
        productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }
        productRepository.deleteById(id);
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
