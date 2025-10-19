package vn.host.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.host.model.response.ProductModel;
import vn.host.model.response.ResponseModel;
import vn.host.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("")
    public ResponseEntity<?> getAllProducts() {
        List<ProductModel> products = productService.findAllProductOrder();
        try {
            return new ResponseEntity<>
                    (
                    new ResponseModel(
                    "Success",
                    "Lấy danh sách sản phẩm thành công",
                    products
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<> (
                    new ResponseModel(
                    "Error",
                    "Lấy danh sách sản phẩm thất bại: " + e.getMessage(),
                    null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }
}
