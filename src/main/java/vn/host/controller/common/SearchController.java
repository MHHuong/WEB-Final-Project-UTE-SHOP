package vn.host.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import vn.host.dto.product.SearchSuggestionDTO;
import vn.host.entity.Product;
import vn.host.service.ProductService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;

    @GetMapping("/api/search/suggest")
    @ResponseBody
    public ResponseEntity<List<SearchSuggestionDTO>> suggest(@RequestParam("q") String q) {
        if (q == null || q.trim().isEmpty()) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(productService.suggest(q, 10));
    }

    // Trang kết quả tìm kiếm
    @GetMapping("/search")
    public String searchPage(@RequestParam(name = "q", required = false, defaultValue = "") String q,
                             @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                             @RequestParam(name = "size", required = false, defaultValue = "12") int size,
                             Model model) {
        Page<Product> result = productService.search(q, PageRequest.of(page, size));
        model.addAttribute("q", q);
        model.addAttribute("page", result);
        return "search/index";
    }
}