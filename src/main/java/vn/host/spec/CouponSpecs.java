package vn.host.spec;

import org.springframework.data.jpa.domain.Specification;
import vn.host.entity.Coupon;

import java.time.LocalDateTime;

public final class CouponSpecs {
    private CouponSpecs() {
    }

    public static Specification<Coupon> belongsToShop(Long shopId) {
        return (root, q, cb) -> cb.equal(root.get("shop").get("shopId"), shopId);
    }

    public static Specification<Coupon> codeContains(String qStr) {
        return (root, q, cb) -> cb.like(cb.lower(root.get("code")), "%" + qStr.toLowerCase() + "%");
    }

    public static Specification<Coupon> statusActive(LocalDateTime now) {
        return (root, q, cb) -> cb.greaterThan(root.get("expiredAt"), now);
    }

    public static Specification<Coupon> statusExpired(LocalDateTime now) {
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("expiredAt"), now);
    }
}
