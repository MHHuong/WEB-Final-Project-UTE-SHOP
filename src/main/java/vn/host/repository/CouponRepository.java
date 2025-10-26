package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Coupon;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,Long> {
    @Query("""
        SELECT c
        FROM Coupon c
        WHERE c.shop IS NULL AND c.expiredAt >= CURRENT_DATE
""")
    List<Coupon> findGlobalCoupon();

    @Query("""
        SELECT c
        FROM Coupon c
        WHERE c.expiredAt >= CURRENT_DATE AND c.shop.shopId = :shopId AND c.shop IS NOT NULL
""")
    List<Coupon> findShopCoupon(Long shopId);

    Optional<Coupon> findByCode(String code);
}
