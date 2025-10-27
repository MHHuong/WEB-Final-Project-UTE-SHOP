package vn.host.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.host.entity.Product; // <-- Import Product
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
        List<Product> allProducts = productService.findAll();
        model.addAttribute("popularProducts", allProducts);
        model.addAttribute("bestSellsProducts", allProducts);
        return "index";
    }
}