package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Order;
import vn.host.model.response.OrderResponse;
import vn.host.util.sharedenum.OrderStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByShop_ShopId(Long shopId, Pageable pageable);

    // Tổng doanh thu trong khoảng theo shop và theo status (đơn đã hoàn tất doanh thu)
    @Query("""
                select coalesce(sum(o.totalAmount), 0)
                from Order o
                where o.shop.shopId = :shopId
                  and o.status in :paidStatuses
                  and o.createdAt >= :from and o.createdAt < :to
            """)
    java.math.BigDecimal sumRevenueInRange(
            @Param("shopId") Long shopId,
            @Param("paidStatuses") List<OrderStatus> paidStatuses,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // Đếm đơn trong khoảng
    @Query("""
                select count(o)
                from Order o
                where o.shop.shopId = :shopId
                  and o.createdAt >= :from and o.createdAt < :to
            """)
    Long countOrdersInRange(
            @Param("shopId") Long shopId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // Đếm khách hàng duy nhất trong khoảng
    @Query("""
                select count(distinct o.user.userId)
                from Order o
                where o.shop.shopId = :shopId
                  and o.createdAt >= :from and o.createdAt < :to
            """)
    Long countUniqueCustomersInRange(
            @Param("shopId") Long shopId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // Đếm status theo khoảng (group by status)
    @Query("""
                select o.status, count(o)
                from Order o
                where o.shop.shopId = :shopId
                  and o.createdAt >= :from and o.createdAt < :to
                group by o.status
            """)
    List<Object[]> countByStatusInRange(
            @Param("shopId") Long shopId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    // Doanh thu theo ngày (30 ngày gần nhất)
    @Query("""
            select cast(o.createdAt as date) as dayKey, coalesce(sum(o.totalAmount), 0)
            from Order o
            where o.shop.shopId = :shopId
              and o.status in :paidStatuses
              and o.createdAt >= :fromTs and o.createdAt < :toTs
            group by cast(o.createdAt as date)
            order by cast(o.createdAt as date)
            """)
    List<Object[]> dailyRevenue(
            @Param("shopId") Long shopId,
            @Param("paidStatuses") List<OrderStatus> paidStatuses,
            @Param("fromTs") Instant fromTs,
            @Param("toTs") Instant toTs
    );

    @Query("""
                select cast(o.createdAt as date) as d, coalesce(sum(o.totalAmount),0)
                from Order o
                where o.shop.shopId = :shopId
                  and o.status = vn.host.util.sharedenum.OrderStatus.RECEIVED
                  and o.createdAt >= :fromTs and o.createdAt < :toTs
                group by cast(o.createdAt as date)
                order by cast(o.createdAt as date)
            """)
    List<Object[]> dailyGross(
            @Param("shopId") Long shopId,
            @Param("fromTs") Instant fromTs,
            @Param("toTs") Instant toTs
    );

    @Query("""
                select cast(o.createdAt as date) as d, coalesce(sum(o.totalAmount),0)
                from Order o
                where o.shop.shopId = :shopId
                  and o.status = vn.host.util.sharedenum.OrderStatus.RETURNED
                  and o.createdAt >= :fromTs and o.createdAt < :toTs
                group by cast(o.createdAt as date)
                order by cast(o.createdAt as date)
            """)
    List<Object[]> dailyReturns(
            @Param("shopId") Long shopId,
            @Param("fromTs") Instant fromTs,
            @Param("toTs") Instant toTs
    );

    @Query("""
                select o
                from Order o
                    left join fetch o.items it
                    left join fetch it.product p
                    left join fetch p.category c
                    left join fetch o.coupon cp
                where o.shop.shopId = :shopId
                  and o.createdAt >= :fromTs and o.createdAt < :toTs
                  and (o.status = vn.host.util.sharedenum.OrderStatus.RECEIVED
                       or o.status = vn.host.util.sharedenum.OrderStatus.RETURNED)
            """)
    List<Order> findForDashboardCalc(
            @Param("shopId") Long shopId,
            @Param("fromTs") Instant fromTs,
            @Param("toTs") Instant toTs
    );

    @Query
            (
                    """
                             SELECT new vn.host.model.response.OrderResponse(
                                o.orderId,
                                o.user.userId,
                                o.shop.shopId,
                                o.paymentMethod,
                                o.totalAmount,
                                o.status,
                                new vn.host.model.request.AddressRequest(
                                    o.address.addressId,
                                    o.address.province,
                                    o.address.district,
                                    o.address.ward,
                                    o.address.addressDetail,
                                    o.address.receiverName,
                                    o.address.phone,
                                    o.address.isDefault
                                ),
                                 o.createdAt,
                                  o.shippingProvider.estimatedDays
                             )
                             FROM Order o
                             WHERE o.user.userId = :userId
                             ORDER BY o.createdAt DESC
                            """
            )
    List<OrderResponse> findAllOrdersByUserId(Long userId);


    @Query
            (
                    """
                             SELECT new vn.host.model.response.OrderResponse(
                                o.orderId,
                                o.user.userId,
                                o.shop.shopId,
                                o.paymentMethod,
                                o.totalAmount,
                                o.status,
                                new vn.host.model.request.AddressRequest(
                                    o.address.addressId,
                                    o.address.province,
                                    o.address.district,
                                    o.address.ward,
                                    o.address.addressDetail,
                                    o.address.receiverName,
                                    o.address.phone,
                                    o.address.isDefault
                                ),
                                 o.createdAt,
                                 o.shippingProvider.estimatedDays
                             )
                             FROM Order o
                             WHERE o.orderId = :orderId
                            """
            )
    OrderResponse findOrderById(Long orderId);

    Order findTopByUser_UserIdAndShop_ShopIdOrderByOrderIdDesc(Long userId, Long shopId);

    List<Order> findAllOrderByAddress_AddressId(Long addressId);
}
