package vn.host.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Coupon;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {
    Page<Coupon> findByShopIsNull(Pageable pageable);

    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);

    Page<Coupon> findByShop_ShopId(Long shopId, Pageable pageable);
}
