package vn.host.dto.dashboard;

import lombok.*;
import vn.host.dto.order.OrderRowVM;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRes {
    // metrics
    private BigDecimal todayRevenue;          // doanh thu hôm nay
    private BigDecimal monthRevenue;          // doanh thu tháng hiện tại
    private Long ordersThisMonth;             // số đơn trong tháng
    private BigDecimal avgOrderValueThisMonth;// AOV tháng
    private Long uniqueCustomersThisMonth;    // số khách hàng unique trong tháng
    private Map<String, Long> statusCounts;   // đếm đơn theo status (tháng)

    // charts / lists
    private List<RevenuePoint> revenueDaily;  // doanh thu từng ngày (30 ngày gần nhất)
    private List<OrderRowVM> recentOrders;    // 10 đơn gần nhất
    private List<TopProductVM> topProducts;   // top sản phẩm theo doanh thu
}
