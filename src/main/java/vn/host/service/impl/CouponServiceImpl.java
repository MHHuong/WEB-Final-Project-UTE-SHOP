package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.entity.Coupon;
import vn.host.repository.CouponRepository;
import vn.host.service.CouponService;

import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    CouponRepository couponRepository;

    @Override
    public List<Coupon> findAllGlobalCoupons() {
        return couponRepository.findGlobalCoupon();
    }

    @Override
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    @Override
    public List<Coupon> findShopCoupons(Long shopId) {
        return couponRepository.findShopCoupon(shopId);
    }
}
