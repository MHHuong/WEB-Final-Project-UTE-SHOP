package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.Order;
import vn.host.entity.Payment;
import vn.host.model.request.AddressRequest;
import vn.host.model.request.OrderItemRequest;
import vn.host.model.response.OrderItemResponse;
import vn.host.model.response.OrderResponse;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
//    Long userId;
//    Long shopId;
//    PaymentMethod paymentMethod;
//    BigDecimal totalAmount;
//    OrderStatus status;
//    String receiverName;
//    String phone;
//    AddressRequest address;
//    Instant createdAt;
    @Query
    (
            """
            SELECT new vn.host.model.response.OrderResponse(
               o.user.userId,
                o.shop.shopId,
               o.paymentMethod,
               o.totalAmount,
               o.status,
               new vn.host.model.request.AddressRequest(
                   o.address.province,
                   o.address.district,
                   o.address.ward,
                   o.address.addressDetail,
                   o.address.receiverName,
                   o.address.phone,
                   o.address.isDefault
               ),
               o.createdAt
            )
            FROM Order o
            WHERE o.user.userId = :userId
            ORDER BY o.createdAt DESC
           """
    )
    List<OrderResponse> findAllOrdersByUserId(Long userId);

    Order findTopByUser_UserIdAndShop_ShopIdOrderByOrderIdDesc(Long userId, Long shopId);
}
