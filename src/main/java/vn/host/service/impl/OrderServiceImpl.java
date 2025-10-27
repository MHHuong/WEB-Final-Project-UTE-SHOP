package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.Order;
import vn.host.repository.OrderRepository;
import vn.host.service.OrderService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public Page<Order> findByShop_ShopId(Long shopId, Pageable pageable) {
        return orderRepository.findByShop_ShopId(shopId, pageable);
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }


}
