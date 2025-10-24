package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Promotion;
import vn.host.service.PromotionService;
import java.util.List;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;

    // Lấy danh sách khuyến mãi toàn hệ thống
    @GetMapping("/global")
    public List<Promotion> getGlobalPromotions() {
        return promotionService.findGlobalPromotions();
    }

    // Tạo mới khuyến mãi toàn hệ thống
    @PostMapping("/global")
    public Promotion createGlobalPromotion(@RequestBody Promotion promotion) {
        promotion.setShop(null); // khuyến mãi toàn hệ thống
        return promotionService.save(promotion);
    }

    // Cập nhật
    @PutMapping("/{id}")
    public Promotion updatePromotion(@PathVariable Long id, @RequestBody Promotion promotion) {
        return promotionService.update(id, promotion);
    }

    // Xóa
    @DeleteMapping("/{id}")
    public void deletePromotion(@PathVariable Long id) {
        promotionService.delete(id);
    }
}
