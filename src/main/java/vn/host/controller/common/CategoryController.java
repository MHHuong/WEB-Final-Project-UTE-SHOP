package vn.host.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.category.CategoryVM;
import vn.host.entity.Category;
import vn.host.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryVM.SimpleItem>> list(
            @RequestParam(required = false) Long parentId
    ) {
        List<Category> categories = (parentId == null)
                ? categoryService.findAll()
                : categoryService.findByParent_CategoryId(parentId);

        List<CategoryVM.SimpleItem> items = categories.stream()
                .map(c -> new CategoryVM.SimpleItem(
                        c.getCategoryId(),
                        c.getName(),
                        c.getParent() != null ? c.getParent().getCategoryId() : null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }
}