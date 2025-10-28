package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.model.response.ProductResponse;
import vn.host.repository.ProductRepository;
import vn.host.service.ProductService;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository productRepository;

    @Override
    public List<ProductResponse> findAllProductOrder() {
        return productRepository.findAllProductsOrder();
    }
}
