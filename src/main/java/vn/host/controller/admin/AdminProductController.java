package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Product;
import vn.host.service.ProductService;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    // 1️⃣ Lấy danh sách sản phẩm
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> result = productService.findAllForAdmin(keyword, categoryId, shopId, status, pageable);
        return ResponseEntity.ok(result);
    }

    // 2️⃣ Lấy chi tiết sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // 3️⃣ Cập nhật trạng thái (ẩn/hiện)
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    // 4️⃣ Xoá sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.ok("Đã xoá sản phẩm");
    }

    @GetMapping("/search/name")
    public ResponseEntity<Page<Product>> searchByName(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productService.searchByName(q, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/shop")
    public ResponseEntity<Page<Product>> searchByShop(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> result = productService.searchByShopName(q, pageable);
        return ResponseEntity.ok(result);
    }
}
