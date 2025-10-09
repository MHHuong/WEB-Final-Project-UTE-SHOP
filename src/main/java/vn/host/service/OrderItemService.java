package vn.host.service;

import vn.host.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    void save(OrderItem orderItem);
    void delete(long id);
    List<OrderItem> findAll();
    OrderItem findById(long id);
}
