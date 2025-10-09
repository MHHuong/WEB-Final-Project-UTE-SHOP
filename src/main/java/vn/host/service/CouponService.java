package vn.host.service;

import vn.host.entity.Coupon;

import java.util.List;

public interface CouponService {
    List<Coupon> findAll();
    Coupon findById(long id);
    void save(Coupon coupon);
    void delete(long id);
}
