package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.dto.shipper.ShipperRequest;
import vn.host.entity.Order;
import vn.host.entity.Shipper;
import vn.host.entity.User;
import vn.host.util.sharedenum.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface ShipperService {
    Page<Shipper> getAll(String keyword, int page, int size);

    Shipper findById(Long id);

    Shipper save(ShipperRequest req);

    Shipper update(Long id, ShipperRequest req);

    void delete(Long id);

    void delete(long id);

    List<Shipper> findAll();

    Shipper findById(long id);

    List<Shipper> findByShippingProviderId(long shippingProviderId);

    Optional<Shipper> findByUserId(Long userId);

    Shipper registerAsShipper(User user, Shipper newInfo);

    Page<Order> listOrdersForShipper(Shipper me, OrderStatus status, Pageable pageable);

    Order pickup(Long orderId, Shipper me);

    Order deliver(Long orderId, Shipper me);

    Shipper edit(Shipper s);

    Page<Order> listReturnPickup(Shipper me, Pageable pageable);

    // Danh sách RETURNED đã lấy hàng trả (đã có RETURN_PICKUP nhưng chưa RETURN_DELIVER) -> lọc theo địa chỉ shop
    Page<Order> listReturnDeliver(Shipper me, Pageable pageable);

    // Đánh dấu đã lấy hàng trả (RETURN_PICKUP)
    Order returnPickup(Long orderId, Shipper me);

    // Đánh dấu đã giao hàng trả về shop (RETURN_DELIVER)
    Order returnDeliver(Long orderId, Shipper me);
}
