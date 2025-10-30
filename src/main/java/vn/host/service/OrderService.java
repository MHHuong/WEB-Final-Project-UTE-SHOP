package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.host.dto.order.OrderReturnResponse;
import vn.host.entity.Order;
import vn.host.entity.Payment;
import vn.host.model.request.OrderRequest;
import vn.host.model.request.ShippingFeeRequest;
import vn.host.model.response.OrderResponse;
import vn.host.model.response.ShippingFeeResponse;
import vn.host.model.response.TempOrderResponse;

import java.util.List;

public interface OrderService {
    List<Order> findAll();

    Page<Order> findAll(Pageable pageable);

    void saveOrder(OrderRequest orderRequest);

    List<OrderResponse> getOrdersByUserId(Long userId);

    OrderResponse getOrderByOrderId(Long orderId);

    void updateStatus(Long orderId, String status, String reason);

    void updatePayment(Long orderId, Payment payment);

    Order findOrderById(long l);

    ShippingFeeResponse calculateShippingFee(ShippingFeeRequest shippingFeeRequest);

    String findTopOrderByUser(TempOrderResponse tempOrderResponse);


    void updateOrderPaymentVnPay(String orderIdsStr, String responseCode, String transNo, Long amount);

    void updateOrderPaymentMomo(String orderIdsStr, Integer responseCode, String transNo, Long amount);

    Page<Order> findByShop_ShopId(Long shopId, Pageable pageable);

    Order findById(Long id);

    void save(Order order);

    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    Page<OrderReturnResponse> findAllReturnOrdersDto(Pageable pageable);

    void updateStatusFast(Long orderId, String newStatus, String note);

    Page<OrderReturnResponse> searchReturnOrdersByCustomer(String keyword, Pageable pageable);
}
