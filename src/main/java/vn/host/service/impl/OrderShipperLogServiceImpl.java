package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.OrderShipperLog;
import vn.host.repository.OrderShipperLogRepository;
import vn.host.service.OrderShipperLogService;
import vn.host.util.sharedenum.ShipperAction;

@Service
@RequiredArgsConstructor
public class OrderShipperLogServiceImpl implements OrderShipperLogService {
    private final OrderShipperLogRepository orderShipperLogRepository;

    @Override
    public Page<OrderShipperLog> findByShipper_ShipperIdAndAction(Long shipperId, ShipperAction action, Pageable pageable) {
        return orderShipperLogRepository.findByShipper_ShipperIdAndAction(shipperId, action, pageable);
    }

    @Override
    public boolean existsAction(long orderId, ShipperAction action) {
        return orderShipperLogRepository.existsByOrder_OrderIdAndAction(orderId, action);
    }

    @Override
    public long countAction(long orderId, ShipperAction action) {
        return orderShipperLogRepository.countByOrder_OrderIdAndAction(orderId, action);
    }
}
