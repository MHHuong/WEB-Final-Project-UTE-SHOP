package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.ProductMedia;
import vn.host.repository.ProductMediaRepository;
import vn.host.service.ProductMediaService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMediaServiceImpl implements ProductMediaService {
    private final ProductMediaRepository productMediaRepository;

    @Override
    public void save(ProductMedia productMedia) {
        productMediaRepository.save(productMedia);
    }

    @Override
    public List<ProductMedia> findByProduct_ProductId(Long productId) {
        return productMediaRepository.findByProduct_ProductId(productId);
    }

    @Override
    public ProductMedia findById(Long id) {
        return productMediaRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(ProductMedia productMedia) {
        productMediaRepository.delete(productMedia);
    }
}
