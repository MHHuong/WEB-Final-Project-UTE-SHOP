package vn.host.service;

import vn.host.entity.ViewedProduct;
import vn.host.entity.ViewedProductId;

import java.util.List;

public interface ViewedProductService {
    void save(ViewedProduct viewedProduct);
    void delete(ViewedProductId viewedProductId);
    List<ViewedProduct> findAll();
    ViewedProduct findById(ViewedProductId viewedProductId);
    List<ViewedProduct> findByUserId(long userId);
}
