package vn.host.service;

import vn.host.entity.Order;
import vn.host.entity.OrderItem;

import java.util.List;

public interface OrderService {
    void save(Order order);
    void delete(long id);
    List<Order> findAll();
    List<OrderItem> findByOrderId(long orderId);
    List<Order> findByUserId(long userId);
    Order findById(long id);
}
