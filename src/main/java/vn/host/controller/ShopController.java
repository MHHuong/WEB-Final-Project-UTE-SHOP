package vn.host.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import vn.host.dto.ProductDTO; // (Import DTO của bạn)
import vn.host.service.ProductService; // (Bạn cần tạo Service này)
import vn.host.service.CategoryService; // (Bạn cần tạo Service này)

import java.util.List;

@Controller
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;
    @GetMapping("/shop")
    public String showShopPage(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productsPage = productService.findAllProducts(pageable);
        List<String> categoryNames = categoryService.getAllCategoryNames();
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("currentCategory", null);
        return "pages/pages/shop-grid";
    }
    @GetMapping("/shop/category/{name}")
    public String showShopCategoryPage(@PathVariable("name") String categoryName,
                                       Model model,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "8") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductDTO> productsPage = productService.findProductsByCategory(categoryName, pageable);
        List<String> categoryNames = categoryService.getAllCategoryNames();

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("currentCategory", categoryName);
        return "pages/pages/shop-grid";
    }
}