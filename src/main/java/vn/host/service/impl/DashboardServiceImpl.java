package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import vn.host.dto.dashboard.*;
import vn.host.dto.order.OrderRowVM;
import vn.host.entity.Order;
import vn.host.repository.*;
import vn.host.service.DashboardService;
import vn.host.util.sharedenum.OrderStatus;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;

    private static final List<OrderStatus> PAID_STATUSES = List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED);

    @Override
    public DashboardRes buildForShop(Long shopId, Instant fromInclusive, Instant toExclusive) {
        // Khung thời gian mặc định: 30 ngày gần nhất nếu caller truyền null
        ZoneId zone = ZoneId.systemDefault();
        Instant now = Instant.now();
        if (fromInclusive == null || toExclusive == null) {
            LocalDate toDate = LocalDate.now(zone).plusDays(1); // exclusive
            LocalDate fromDate = toDate.minusDays(30);
            fromInclusive = fromDate.atStartOfDay(zone).toInstant();
            toExclusive = toDate.atStartOfDay(zone).toInstant();
        }

        // Doanh thu hôm nay
        LocalDate today = LocalDate.now(zone);
        Instant startToday = today.atStartOfDay(zone).toInstant();
        Instant endToday = today.plusDays(1).atStartOfDay(zone).toInstant();
        BigDecimal todayRevenue = nvl(orderRepo.sumRevenueInRange(shopId, PAID_STATUSES, startToday, endToday));

        // Doanh thu tháng hiện tại
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        Instant monthStart = firstOfMonth.atStartOfDay(zone).toInstant();
        Instant monthEnd = firstOfMonth.plusMonths(1).atStartOfDay(zone).toInstant();
        BigDecimal monthRevenue = nvl(orderRepo.sumRevenueInRange(shopId, PAID_STATUSES, monthStart, monthEnd));

        Long ordersThisMonth = orderRepo.countOrdersInRange(shopId, monthStart, monthEnd);
        if (ordersThisMonth == null) ordersThisMonth = 0L;

        BigDecimal avgOrderValue = ordersThisMonth == 0 ? BigDecimal.ZERO :
                monthRevenue.divide(new BigDecimal(ordersThisMonth), java.math.MathContext.DECIMAL64);

        Long uniqueCustomers = orderRepo.countUniqueCustomersInRange(shopId, monthStart, monthEnd);
        if (uniqueCustomers == null) uniqueCustomers = 0L;

        // Đếm status theo tháng
        EnumMap<OrderStatus, Long> stMap = new EnumMap<>(OrderStatus.class);
        orderRepo.countByStatusInRange(shopId, monthStart, monthEnd).forEach(arr -> {
            OrderStatus st = (OrderStatus) arr[0];
            Long count = (Long) arr[1];
            if (st != null && count != null) {
                stMap.merge(st, count, Long::sum);
            }
        });

        Map<String, Long> statusCounts =
                stMap.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                e -> e.getKey().name(),
                                java.util.Map.Entry::getValue,
                                (a, b) -> a,
                                java.util.LinkedHashMap::new
                        ));

        // Doanh thu theo ngày (30 ngày gần nhất)
        List<RevenuePoint> revenueDaily = new ArrayList<>();
        orderRepo.dailyRevenue(shopId, PAID_STATUSES, fromInclusive, toExclusive).forEach(arr -> {
            java.sql.Date d = (java.sql.Date) arr[0];
            BigDecimal amount = (BigDecimal) arr[1];
            revenueDaily.add(RevenuePoint.builder()
                    .date(d.toLocalDate())
                    .amount(nvl(amount))
                    .build());
        });

        // 10 đơn gần nhất
        Page<Order> recent = orderRepo.findByShop_ShopId(shopId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<OrderRowVM> recentOrders = recent.getContent().stream().map(OrderRowVM::of).toList();

        // Top 5 sản phẩm theo doanh thu trong tháng
        List<TopProductVM> topProducts = orderItemRepo.topProductsByRevenue(shopId, PAID_STATUSES, monthStart, monthEnd)
                .stream().limit(5).map(arr -> TopProductVM.builder()
                        .productId((Long) arr[0])
                        .name((String) arr[1])
                        .totalQty(((Number) arr[2]).longValue())
                        .revenue(nvl((BigDecimal) arr[3]))
                        .build()
                ).collect(Collectors.toList());

        return DashboardRes.builder()
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .ordersThisMonth(ordersThisMonth)
                .avgOrderValueThisMonth(avgOrderValue)
                .uniqueCustomersThisMonth(uniqueCustomers)
                .statusCounts(statusCounts)
                .revenueDaily(revenueDaily)
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
