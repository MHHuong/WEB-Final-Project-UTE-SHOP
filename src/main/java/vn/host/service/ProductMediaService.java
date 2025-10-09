package vn.host.service;

import vn.host.entity.ProductMedia;

import java.util.List;

public interface ProductMediaService {
    void save(ProductMedia productMedia);
    void delete(long id);
    List<ProductMedia> findAll();
    ProductMedia findById(long id);
}
