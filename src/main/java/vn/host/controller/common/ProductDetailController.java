package vn.host.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.host.dto.product.ProductDetailVM;
import vn.host.dto.review.ReviewItemRes;
import vn.host.service.ProductService;
import vn.host.service.ReviewService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductDetailController {
    private final ProductService productService;
    private final ReviewService reviewService;
    @GetMapping("/products/{id}")
    public String viewProductDetail(
            @PathVariable("id") Long productId,
            @RequestParam(defaultValue = "0") int reviewPage,
            @RequestParam(defaultValue = "5") int reviewSize,
            Model model
    ) {
        ProductDetailVM productVM = productService.getProductDetailVM(productId);
        Pageable reviewPageable = PageRequest.of(reviewPage, reviewSize);
        List<ReviewItemRes> reviews = reviewService.getReviewsByProductIdVM(productId, reviewPageable);
        model.addAttribute("product", productVM);
        model.addAttribute("reviews", reviews);
        return "common/product-detail";
    }
}