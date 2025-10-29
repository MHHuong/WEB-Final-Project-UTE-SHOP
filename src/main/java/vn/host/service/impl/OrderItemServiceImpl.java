package vn.host.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.model.response.OrderItemResponse;
import vn.host.repository.OrderItemRepository;
import vn.host.service.OrderItemService;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    OrderItemRepository orderDetailRepository;

    @Override
    public List<OrderItemResponse> getOrderDetailsByOrderIdAndUserId(Long orderId, Long userId) {
        return orderDetailRepository.findOrderByOrderIdAndUserId(orderId, userId);
    }
}
