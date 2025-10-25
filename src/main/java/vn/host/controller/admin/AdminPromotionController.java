package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.host.entity.Category;
import vn.host.entity.Promotion;
import vn.host.repository.CategoryRepository;
import vn.host.service.PromotionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {
    private final PromotionService promotionService;
    private final CategoryRepository categoryRepository;

    // ===================== GET LIST =====================
    @GetMapping
    public ResponseEntity<Page<Promotion>> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Promotion> result = promotionService.getAll(keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    // ===================== CREATE NEW =====================
    @PostMapping
    public ResponseEntity<String> createPromotion(@RequestBody Map<String, Object> body) {
        try {
            Promotion saved = promotionService.save(body);
            return ResponseEntity.ok("Promotion created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // ===================== UPDATE =====================
    @PutMapping("/{id}")
    public ResponseEntity<String> updatePromotion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        try {
            promotionService.update(id, body);
            return ResponseEntity.ok("Promotion updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===================== DELETE =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            promotionService.delete(id);
            return ResponseEntity.ok("Promotion deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===================== ACTIVE PROMOTIONS =====================
    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions(@RequestParam(required = false) Long categoryId) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        List<Promotion> promotions = promotionService.getActivePromotions(category);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Promotion promotion = promotionService.findById(id);
            return ResponseEntity.ok(promotion);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body("Không tìm thấy chương trình khuyến mãi với ID = " + id);
        }
    }


}
