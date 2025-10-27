package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Order;
import vn.host.entity.OrderItem;

import java.util.List;

public interface OrderService {
    Page<Order> findByShop_ShopId(Long shopId, Pageable pageable);

    Order findById(Long id);

    void save(Order order);
}
