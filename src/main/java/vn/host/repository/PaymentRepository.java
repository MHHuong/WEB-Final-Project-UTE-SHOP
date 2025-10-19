package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.host.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
