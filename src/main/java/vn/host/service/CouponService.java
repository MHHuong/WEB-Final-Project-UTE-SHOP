package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Coupon;

import java.util.List;

public interface CouponService {
    Page<Coupon> findAppCoupons(Pageable pageable);
    Page<Coupon> searchByCode(String code, Pageable pageable);
    Coupon save(Coupon coupon);
    void delete(Long id);
}
