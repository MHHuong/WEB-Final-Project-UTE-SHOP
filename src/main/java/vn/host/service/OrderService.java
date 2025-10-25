package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Order;
import vn.host.entity.Payment;
import vn.host.model.request.OrderRequest;
import vn.host.model.request.ShippingFeeRequest;
import vn.host.model.response.OrderResponse;

import java.util.List;

public interface OrderService {
    List<Order> findAll();

    Page<Order> findAll(Pageable pageable);

    void saveOrder(OrderRequest orderRequest);

    List<OrderResponse> getOrdersByUserId(Long userId);

    void updateStatus(Long orderId, String status);

    void updatePayment(Long orderId, Payment payment);

    Order findOrderById(long l);

    Double calculateShippingFee(ShippingFeeRequest shippingFeeRequest);
}
