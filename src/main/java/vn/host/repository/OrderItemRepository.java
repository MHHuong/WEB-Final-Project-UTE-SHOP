package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.OrderItem;
import vn.host.model.response.OrderItemResponse;
import vn.host.util.sharedenum.OrderStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {
    @Query("""
                select
                  oi.product.productId,
                  oi.product.name,
                  sum(oi.quantity),
                  sum(oi.unitPrice * oi.quantity) - coalesce(sum(oi.discountAmount), 0)
                from OrderItem oi
                  join oi.order o
                where o.shop.shopId = :shopId
                  and o.status in :paidStatuses
                  and o.createdAt >= :from and o.createdAt < :to
                group by oi.product.productId, oi.product.name
                order by (sum(oi.unitPrice * oi.quantity) - coalesce(sum(oi.discountAmount), 0)) desc
            """)
    List<Object[]> topProductsByRevenue(
            @Param("shopId") Long shopId,
            @Param("paidStatuses") List<OrderStatus> paidStatuses,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

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
