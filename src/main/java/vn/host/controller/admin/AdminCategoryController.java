package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Category;
import vn.host.service.CategoryService;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AdminCategoryController {

    private final CategoryService categoryService;

    // ✅ Lấy danh sách tất cả category (phân trang)
    @GetMapping
    public Page<Category> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryService.findAll(pageable);
    }

    // ✅ Tìm theo ID
    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
    }

    // ✅ Tìm theo tên (ignore case + phân trang)
    @GetMapping("/search")
    public Page<Category> search(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryService.searchByName(keyword, pageable);
    }

    // ✅ Tạo mới
    @PostMapping
    public Category create(@RequestBody Category category) {
        return categoryService.save(category);
    }

    // ✅ Cập nhật
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category category) {
        category.setCategoryId(id);
        return categoryService.save(category);
    }

    // ✅ Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.ok("Xóa danh mục thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
