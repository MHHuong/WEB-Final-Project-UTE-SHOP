package vn.host.service;

import vn.host.entity.OrderItem;
import vn.host.model.response.OrderItemResponse;

import java.util.List;

public interface OrderItemService {
    List<OrderItemResponse> getOrderDetailsByOrderIdAndUserId(Long orderId, Long userId);
}
