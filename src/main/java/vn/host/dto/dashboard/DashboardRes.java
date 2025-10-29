package vn.host.dto.dashboard;

import lombok.*;
import vn.host.dto.order.OrderRowVM;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRes {
    // summary cards
    private BigDecimal todayRevenue;                  // doanh thu ròng hôm nay
    private BigDecimal monthRevenue;                  // doanh thu ròng tháng hiện tại
    private Long ordersThisMonth;                // số đơn trong tháng
    private BigDecimal avgOrderValueThisMonth;        // AOV tháng
    private Long uniqueCustomersThisMonth;       // khách hàng unique
    private Map<String, Long> statusCounts;           // đếm đơn theo trạng thái (tháng)

    // tổng gộp/nét theo khoảng lọc
    private BigDecimal totalGross;    // tổng tiền bán (RECEIVED)
    private BigDecimal totalSalesFee; // tổng phí sales (promo+coupon của shop)
    private BigDecimal totalReturns;  // tổng hàng trả lại (RETURNED)
    private BigDecimal totalNet;      // = gross - salesFee - returns

    // charts / lists
    private List<RevenuePoint> revenueDaily;          // chuỗi 30 ngày hoặc theo filter
    private List<OrderRowVM> recentOrders;            // 10 đơn gần nhất
    private List<TopProductVM> topProducts;           // top theo doanh thu
}
