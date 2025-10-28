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
import vn.host.dto.CategoryNodeDTO; // Import DTO mới
import vn.host.dto.ProductDTO;
import vn.host.service.ProductService;
import vn.host.service.CategoryService;

import java.util.List;
import java.util.Set; // Import Set

@Controller
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Hiển thị trang shop chính (tất cả sản phẩm)
     */
    @GetMapping("/shop")
    public String showShopPage(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size) { // Giữ size = 8
        Pageable pageable = PageRequest.of(page, size);

        // Lấy dữ liệu sản phẩm
        Page<ProductDTO> productsPage = productService.findAllProducts(pageable);

        // Lấy dữ liệu cho sidebar dạng cây
        List<CategoryNodeDTO> categoryTree = categoryService.getCategoryTree();
        // Khi xem tất cả, không có ID nào được chọn hoặc cần mở rộng
        Set<Long> expandedCategoryIds = categoryService.getExpandedCategoryIds(null);
        Long currentCategoryId = null;

        // Thêm dữ liệu vào Model
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("expandedCategoryIds", expandedCategoryIds);
        model.addAttribute("currentCategoryId", currentCategoryId);

        return "pages/pages/shop-grid"; // Đường dẫn đến template
    }

    /**
     * Hiển thị trang shop theo ID danh mục
     */
    // SỬA: Đổi {name} thành {id} và kiểu dữ liệu thành Long
    @GetMapping("/shop/category/{id}")
    public String showShopCategoryPage(@PathVariable("id") Long categoryId,
                                       Model model,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "8") int size) { // Giữ size = 8

        Pageable pageable = PageRequest.of(page, size);

        // SỬA: Lấy sản phẩm theo category ID
        // !!! Yêu cầu: ProductService phải có phương thức này !!!
        Page<ProductDTO> productsPage = productService.findProductsByCategoryId(categoryId, pageable);

        // Lấy dữ liệu cho sidebar dạng cây
        List<CategoryNodeDTO> categoryTree = categoryService.getCategoryTree();
        // Lấy ID của danh mục hiện tại và các cha của nó để mở rộng
        Set<Long> expandedCategoryIds = categoryService.getExpandedCategoryIds(categoryId);
        Long currentCategoryId = categoryId; // Lưu ID hiện tại

        // Thêm dữ liệu vào Model
        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("expandedCategoryIds", expandedCategoryIds);
        model.addAttribute("currentCategoryId", currentCategoryId);

        return "pages/pages/shop-grid"; // Đường dẫn đến template
    }
}