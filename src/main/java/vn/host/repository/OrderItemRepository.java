package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.OrderItem;
import vn.host.model.response.OrderItemResponse;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
            SELECT new vn.host.model.response.OrderItemResponse(
                oi.product.productId,
                oi.product.name,
                oi.quantity,
                oi.unitPrice,
                oi.discountAmount
            )
            FROM OrderItem oi
            WHERE oi.order.orderId = :orderId
                AND oi.order.user.userId = :userId
            """)
    public List<OrderItemResponse> findOrderByOrderIdAndUserId(Long orderId, Long userId);
}
