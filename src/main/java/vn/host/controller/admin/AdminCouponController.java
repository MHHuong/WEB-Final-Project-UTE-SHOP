package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Coupon;
import vn.host.service.CouponService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/app")
    public Page<Coupon> getAppCoupons(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return couponService.findAppCoupons(PageRequest.of(page, size));
    }

    @GetMapping("/search")
    public Page<Coupon> search(@RequestParam String code,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return couponService.searchByCode(code, PageRequest.of(page, size));
    }

    @PostMapping("/app")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        try {
            Coupon saved = couponService.save(coupon);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi thêm coupon: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
        public ResponseEntity<String> delete(@PathVariable Long id) {
            try {
                couponService.delete(id);
                return ResponseEntity.ok("Coupon deleted successfully!");
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Lỗi khi xóa coupon: " + e.getMessage());
            }
        }
}
