package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.host.dto.common.PageResult;
import vn.host.dto.coupon.CouponVM;
import vn.host.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponService {
    Coupon findById(long id);

    void delete(long id);

    Optional<Coupon> findByCode(String code);

    Page<Coupon> findByShop_ShopId(long shopId, Pageable pageable);

    PageResult<CouponVM> searchOwnerCoupons(String userEmail, String q, String status, int page, int size, Sort sort);

    Page<Coupon> findAppCoupons(Pageable pageable);

    Page<Coupon> searchByCode(String code, Pageable pageable);

    Coupon save(Coupon coupon);

    void delete(Long id);

    List<Coupon> findAllGlobalCoupons();

    List<Coupon> findAll();

    List<Coupon> findShopCoupons(Long shopId);
}
