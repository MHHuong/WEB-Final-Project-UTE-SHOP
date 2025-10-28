package vn.host.service;

import vn.host.model.response.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> findAllProductOrder();
}
