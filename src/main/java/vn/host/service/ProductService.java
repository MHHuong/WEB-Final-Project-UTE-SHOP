package vn.host.service;

import vn.host.model.response.ProductModel;

import java.util.List;

public interface ProductService {
    List<ProductModel> findAllProductOrder();
}
