package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.model.response.ProductModel;
import vn.host.repository.ProductRespository;
import vn.host.service.ProductService;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRespository productRespository;

    @Override
    public List<ProductModel> findAllProductOrder() {
        return productRespository.findAllProductsOrder();
    }
}
