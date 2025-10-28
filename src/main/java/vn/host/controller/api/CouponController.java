package vn.host.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.host.entity.Coupon;
import vn.host.model.response.ApiResponse;
import vn.host.service.CouponService;

import java.util.List;

@RestController
@RequestMapping("api/coupons")
public class CouponController {

    @Autowired
    CouponService couponService;

    @GetMapping("/global")
    public ResponseEntity<?> getGlobalCoupon() {
        try {
            List<Coupon> coupons = couponService.findAllGlobalCoupons();
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Lấy danh sách mã giảm giá toàn cục thành công",
                            coupons
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Lấy danh sách mã giảm giá toàn cục thất bại: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getShopCoupons(@PathVariable Long shopId) {
        try {
            List<Coupon> coupons = couponService.findShopCoupons(shopId); // Replace with actual method to get shop-specific coupons
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Lấy danh sách mã giảm giá của cửa hàng thành công",
                            coupons
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Error",
                            "Lấy danh sách mã giảm giá của cửa hàng thất bại: " + e.getMessage(),
                            null
                    ), HttpStatus.BAD_REQUEST
            );
        }
    }
}
