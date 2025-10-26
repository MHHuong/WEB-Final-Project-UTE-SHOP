package vn.host.service;

import vn.host.entity.Coupon;

import java.util.List;

public interface CouponService {
    List<Coupon> findAllGlobalCoupons();

    List<Coupon> findAll();

    List<Coupon> findShopCoupons(Long shopId);
}
