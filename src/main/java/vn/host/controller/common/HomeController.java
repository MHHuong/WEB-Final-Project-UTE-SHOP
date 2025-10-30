package vn.host.controller.common;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String home(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductDTO> popularPage = productService.findActiveProductsAsDTO(page, size);

        model.addAttribute("popularProducts", popularPage.getContent());
        model.addAttribute("popularPage", popularPage);
        List<ProductDTO> allProductsDTO = productService.findAllProductsAsDTO();

        model.addAttribute("bestSellsProducts", allProductsDTO);
        return "index";
    }
}