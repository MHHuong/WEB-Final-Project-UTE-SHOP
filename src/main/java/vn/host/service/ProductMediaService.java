package vn.host.service;

import vn.host.entity.ProductMedia;

import java.util.List;

public interface ProductMediaService {
    void save(ProductMedia productMedia);

    List<ProductMedia> findByProduct_ProductId(Long productId);
}
