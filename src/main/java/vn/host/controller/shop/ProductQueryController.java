package vn.host.controller.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductListItemVM;
import vn.host.service.ProductService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shop/products")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductService browseService;

    @GetMapping
    public ResponseEntity<PageResult<ProductListItemVM>> search(
            Authentication auth,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort
    ) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Unauthenticated");

        Sort sortObj = parseSort(sort);
        var result = browseService.searchOwnerProducts(
                auth.getName(), q, categoryId, status, minPrice, maxPrice, page, size, sortObj
        );
        return ResponseEntity.ok(result);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "price" -> Sort.by(dir, "price");
            case "name" -> Sort.by(dir, "name");
            case "stock" -> Sort.by(dir, "stock");
            default -> Sort.by(dir, "createdAt");
        };
    }
}