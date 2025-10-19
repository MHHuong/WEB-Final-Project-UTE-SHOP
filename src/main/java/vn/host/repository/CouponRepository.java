package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.host.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,Long> {
}
