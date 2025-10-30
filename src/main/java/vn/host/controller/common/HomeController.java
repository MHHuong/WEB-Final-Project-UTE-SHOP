package vn.host.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.host.dto.common.ProductDTO;
import vn.host.service.ProductService;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<ProductDTO> allProductsDTO = productService.findAllProductsAsDTO();
        model.addAttribute("popularProducts", allProductsDTO);
        model.addAttribute("bestSellsProducts", allProductsDTO);
        return "index";
    }
}