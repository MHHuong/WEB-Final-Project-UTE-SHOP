package vn.host.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.entity.Product;
import vn.host.repository.ProductRepository; // (File này ở mục 2)
import vn.host.service.ProductService;

import java.util.List;

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
}