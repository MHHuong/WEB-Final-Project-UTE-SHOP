package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.OrderShipperLog;
import vn.host.util.sharedenum.ShipperAction;

public interface OrderShipperLogService {
    Page<OrderShipperLog> findByShipper_ShipperIdAndAction(Long shipperId, ShipperAction action, Pageable pageable);

    boolean existsAction(long orderId, ShipperAction action);

    long countAction(long orderId, ShipperAction action);
}
