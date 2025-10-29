package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Order;
import vn.host.entity.Shipper;
import vn.host.entity.User;
import vn.host.util.sharedenum.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface ShipperService {
    Page<Shipper> getAll(String keyword, int page, int size);

    Shipper findById(Long id);

    Shipper save(Shipper shipper);

    Shipper update(Long id, Shipper shipper);

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
}
