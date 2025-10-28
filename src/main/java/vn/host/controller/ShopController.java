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
import vn.host.dto.CategoryNodeDTO;
import vn.host.dto.ProductDTO;
import vn.host.service.ProductService;
import vn.host.service.CategoryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Controller
public class ShopController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/shop-grid")
    public String showShopPage(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               @RequestParam(required = false) BigDecimal minPrice,
                               @RequestParam(required = false) BigDecimal maxPrice) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productsPage = productService.findAllProductsFiltered(minPrice, maxPrice, pageable);
        List<CategoryNodeDTO> categoryTree = categoryService.getCategoryTree();
        Set<Long> expandedCategoryIds = categoryService.getExpandedCategoryIds(null);
        Long currentCategoryId = null;
        String currentCategoryName = null;
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("expandedCategoryIds", expandedCategoryIds);
        model.addAttribute("currentCategoryId", currentCategoryId);
        model.addAttribute("currentCategoryName", currentCategoryName);
        return "shop/common/shop-grid";
    }
    @GetMapping("/shop/category/{id}")
    public String showShopCategoryPage(@PathVariable("id") Long categoryId,
                                       Model model,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "8") int size,
                                       @RequestParam(required = false) BigDecimal minPrice,
                                       @RequestParam(required = false) BigDecimal maxPrice) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productsPage = productService.findProductsByCategoryIdFiltered(categoryId, minPrice, maxPrice, pageable);
        List<CategoryNodeDTO> categoryTree = categoryService.getCategoryTree();
        Set<Long> expandedCategoryIds = categoryService.getExpandedCategoryIds(categoryId);
        Long currentCategoryId = categoryId;
        String currentCategoryName = categoryService.getCategoryNameById(categoryId);
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("expandedCategoryIds", expandedCategoryIds);
        model.addAttribute("currentCategoryId", currentCategoryId);
        model.addAttribute("currentCategoryName", currentCategoryName);
        return "shop/common/shop-grid";
    }
}