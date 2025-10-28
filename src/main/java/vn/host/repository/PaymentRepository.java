package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.host.entity.Payment;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Set<Payment> findByOrder_OrderId(Long orderId);
}
