package vn.host.service;

import org.springframework.data.domain.Page;
import vn.host.entity.Order;
import vn.host.entity.Shipper;

import java.util.List;

public interface ShipperService {
    Page<Shipper> getAll(String keyword, int page, int size);
    Shipper findById(Long id);
    Shipper save(Shipper shipper);
    Shipper update(Long id, Shipper shipper);
    void delete(Long id);
    Page<Order> getUnassignedOrders(int page, int size);
    Order assignOrderToShipper(Long orderId, Long shipperId);
}
