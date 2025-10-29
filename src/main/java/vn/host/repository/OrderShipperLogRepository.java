package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.host.entity.OrderShipperLog;
import vn.host.util.sharedenum.ShipperAction;

public interface OrderShipperLogRepository extends JpaRepository<OrderShipperLog, Long> {
    Page<OrderShipperLog> findByShipper_ShipperIdAndAction(Long shipperId, ShipperAction action, Pageable pageable);

    boolean existsByOrder_OrderIdAndAction(Long orderId, ShipperAction action);

    long countByOrder_OrderIdAndAction(Long orderId, ShipperAction action);
}
