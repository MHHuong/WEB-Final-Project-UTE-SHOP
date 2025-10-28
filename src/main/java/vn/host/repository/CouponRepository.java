package vn.host.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Coupon;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {
    Page<Coupon> findByShopIsNull(Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);

    Page<Coupon> findByShop_ShopId(Long shopId, Pageable pageable);

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
}
