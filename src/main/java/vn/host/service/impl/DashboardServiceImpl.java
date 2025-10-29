package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.host.dto.dashboard.*;
import vn.host.dto.order.OrderRowVM;
import vn.host.entity.*;
import vn.host.repository.*;
import vn.host.service.DashboardService;
import vn.host.util.sharedenum.DiscountType;
import vn.host.util.sharedenum.OrderStatus;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final PromotionRepository promotions;

    @Override
    public DashboardRes buildForShop(Long shopId, Instant fromInclusive, Instant toExclusive) {
        // 1) Khoảng thời gian mặc định: 30 ngày gần nhất (đồng thời build combobox năm sẽ gọi lại API)
        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        if (fromInclusive == null || toExclusive == null) {
            LocalDate today = LocalDate.now(zone);
            LocalDate start = today.minusDays(29);
            fromInclusive = start.atStartOfDay(zone).toInstant();
            toExclusive = today.plusDays(1).atStartOfDay(zone).toInstant();
        }

        // 2) Lấy dữ liệu thô cho tính toán
        List<Order> forCalc = orders.findForDashboardCalc(shopId, fromInclusive, toExclusive);

        // Chuẩn bị map daily
        Map<LocalDate, RevenuePoint> daily = new LinkedHashMap<>();
        LocalDate startDate = LocalDateTime.ofInstant(fromInclusive, zone).toLocalDate();
        LocalDate endDate = LocalDateTime.ofInstant(toExclusive, zone).toLocalDate().minusDays(1);
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            daily.put(d, RevenuePoint.builder()
                    .date(d)
                    .gross(BigDecimal.ZERO)
                    .salesFee(BigDecimal.ZERO)
                    .returns(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());
        }

        // 3) Chuẩn bị promotions của shop active trong range (để nhận diện discount do shop)
        List<Promotion> shopPromos = promotions.findActiveInRangeForShop(
                shopId, startDate, endDate);
        // Index nhanh theo categoryId để khớp sản phẩm
        Set<Long> promoCategoryIds = shopPromos.stream()
                .map(p -> p.getApplyCategory() == null ? null : p.getApplyCategory().getCategoryId())
                .collect(Collectors.toSet());

        // 4) Tính tổng & daily
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalReturns = BigDecimal.ZERO;
        BigDecimal totalSalesFee = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Order o : forCalc) {
            LocalDate d = LocalDateTime.ofInstant(o.getCreatedAt(), zone).toLocalDate();
            RevenuePoint pt = daily.getOrDefault(d, RevenuePoint.builder()
                    .date(d).gross(BigDecimal.ZERO).salesFee(BigDecimal.ZERO)
                    .returns(BigDecimal.ZERO).net(BigDecimal.ZERO).build());

            boolean isReceived = o.getStatus() == OrderStatus.RECEIVED;
            boolean isReturned = o.getStatus() == OrderStatus.RETURNED;

            // Gross chỉ tính RECEIVED
            BigDecimal gross = isReceived ? nvl(o.getTotalAmount()) : BigDecimal.ZERO;

            // Returns: tổng tiền các đơn RETURNED trong ngày
            BigDecimal returns = isReturned ? nvl(o.getTotalAmount()) : BigDecimal.ZERO;

            // Sales fee = discount do shop chịu:
            // (A) Từ promotion: cộng các OrderItem.discountAmount nếu item thuộc category đang có promotion của shop tại thời điểm đơn
            BigDecimal promoFee = BigDecimal.ZERO;
            for (OrderItem it : o.getItems()) {
                Long catId = it.getProduct() != null && it.getProduct().getCategory() != null
                        ? it.getProduct().getCategory().getCategoryId() : null;

                // Nếu shop có promotion áp trên category này (không cần xác minh id cụ thể vì entity không lưu track),
                // coi toàn bộ discountAmount là giảm giá do shop.
                if (promoCategoryIds.contains(catId) || promoCategoryIds.contains(null)) {
                    promoFee = promoFee.add(nvl(it.getDiscountAmount()));
                }
            }

            // (B) Từ coupon của shop: nếu order.coupon != null và coupon.shopId = shopId
            BigDecimal couponFee = BigDecimal.ZERO;
            if (o.getCoupon() != null && o.getCoupon().getShop() != null
                    && Objects.equals(o.getCoupon().getShop().getShopId(), shopId)) {

                // Tính "trước coupon"
                BigDecimal beforeCoupon = BigDecimal.ZERO;
                for (OrderItem it : o.getItems()) {
                    BigDecimal line = nvl(it.getUnitPrice()).multiply(BigDecimal.valueOf(it.getQuantity()));
                    line = line.subtract(nvl(it.getDiscountAmount())); // trừ discount item (có thể là promotion)
                    beforeCoupon = beforeCoupon.add(line);
                }

                Coupon cp = o.getCoupon();
                if (beforeCoupon.compareTo(nvl(cp.getMinOrderAmount())) >= 0) {
                    if (cp.getDiscountType() == DiscountType.PERCENT) {
                        // value là % (ví dụ 10 = 10%)
                        BigDecimal pct = nvl(cp.getValue()).movePointLeft(2); // /100
                        couponFee = beforeCoupon.multiply(pct);
                    } else {
                        // AMOUNT = số tiền cố định
                        couponFee = nvl(cp.getValue());
                    }
                    // Không vượt quá beforeCoupon
                    if (couponFee.compareTo(beforeCoupon) > 0) couponFee = beforeCoupon;
                }
            }

            BigDecimal salesFee = promoFee.add(couponFee);
            // Với yêu cầu: phí chỉ trừ khi order RECEIVED (đã bán ra); còn RETURNED là khoản trừ riêng
            if (!isReceived) {
                salesFee = BigDecimal.ZERO;
            }

            BigDecimal net = gross.subtract(salesFee).subtract(returns);

            // gộp vào ngày
            pt.setGross(pt.getGross().add(gross));
            pt.setSalesFee(pt.getSalesFee().add(salesFee));
            pt.setReturns(pt.getReturns().add(returns));
            pt.setNet(pt.getNet().add(net));
            daily.put(d, pt);

            // gộp tổng
            totalGross = totalGross.add(gross);
            totalSalesFee = totalSalesFee.add(salesFee);
            totalReturns = totalReturns.add(returns);
            totalNet = totalNet.add(net);
        }

        // 5) Các thẻ summary sẵn có (giữ nguyên phong cách cũ)
        // todayRevenue = ròng hôm nay
        LocalDate today = LocalDate.now(zone);
        BigDecimal todayRevenue = daily.getOrDefault(today,
                RevenuePoint.builder().net(BigDecimal.ZERO).build()).getNet();

        // monthRevenue, ordersThisMonth, avgOrderValueThisMonth, uniqueCustomersThisMonth, statusCounts
        // (tận dụng logic cũ nếu có; ở đây tính gọn)
        LocalDate firstDay = today.withDayOfMonth(1);
        Instant monthStart = firstDay.atStartOfDay(zone).toInstant();
        Instant monthEnd = firstDay.plusMonths(1).atStartOfDay(zone).toInstant();
        List<Order> monthOrders = orders.findForDashboardCalc(shopId, monthStart, monthEnd);

        long ordersThisMonth = monthOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.RECEIVED)
                .count();

        BigDecimal totalReceivedMonth = monthOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.RECEIVED)
                .map(o -> nvl(o.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = ordersThisMonth == 0
                ? BigDecimal.ZERO
                : totalReceivedMonth.divide(BigDecimal.valueOf(ordersThisMonth), 2, java.math.RoundingMode.HALF_UP);

        long uniqueCustomers = monthOrders.stream()
                .map(o -> o.getUser() != null ? o.getUser().getUserId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()).size();

        Map<String, Long> statusCounts = monthOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Recent orders & top products (giữ nguyên cách cũ nếu đã có)
        List<OrderRowVM> recentOrders = monthOrders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .map(o -> OrderRowVM.of(o))
                .collect(Collectors.toList());

        List<Object[]> tops = orderItems.topProductsByRevenue(
                shopId,
                List.of(OrderStatus.RECEIVED),
                fromInclusive,
                toExclusive
        );
        List<TopProductVM> topProducts = tops.stream().map(arr -> {
            // arr: [productId, productName, grossRevenue]
            Long pid = ((Number) arr[0]).longValue();
            String name = (String) arr[1];
            BigDecimal rev = toBigDecimal(arr[2]);
            return TopProductVM.builder().productId(pid).name(name).revenue(nvl(rev)).build();
        }).collect(Collectors.toList());

        return DashboardRes.builder()
                // summary
                .todayRevenue(nvl(todayRevenue))
                .monthRevenue(nvl(totalNetForMonth(monthOrders, shopId)))
                .ordersThisMonth(ordersThisMonth)
                .avgOrderValueThisMonth(nvl(avgOrderValue))
                .uniqueCustomersThisMonth(uniqueCustomers)
                .statusCounts(statusCounts)

                // totals theo filter
                .totalGross(nvl(totalGross))
                .totalSalesFee(nvl(totalSalesFee))
                .totalReturns(nvl(totalReturns))
                .totalNet(nvl(totalNet))

                // series & lists
                .revenueDaily(new ArrayList<>(daily.values()))
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) {
            if (n instanceof Long || n instanceof Integer || n instanceof Short)
                return BigDecimal.valueOf(n.longValue());
            return BigDecimal.valueOf(n.doubleValue());
        }
        if (o instanceof String s && !s.isBlank()) return new BigDecimal(s);
        return BigDecimal.ZERO;
    }

    // Tính ròng của cả tháng hiện tại (để điền monthRevenue)
    private BigDecimal totalNetForMonth(List<Order> monthOrders, Long shopId) {
        BigDecimal gross = BigDecimal.ZERO, returns = BigDecimal.ZERO, fees = BigDecimal.ZERO;
        for (Order o : monthOrders) {
            if (o.getStatus() == OrderStatus.RECEIVED) gross = gross.add(nvl(o.getTotalAmount()));
            if (o.getStatus() == OrderStatus.RETURNED) returns = returns.add(nvl(o.getTotalAmount()));
            // Phí sales tháng (chỉ tính RECEIVED); tính đơn giản hơn – tái sử dụng lại kiểu tính như trên nếu cần
            if (o.getStatus() == OrderStatus.RECEIVED) {
                BigDecimal promoFee = o.getItems().stream()
                        .map(OrderItem::getDiscountAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal couponFee = BigDecimal.ZERO;
                if (o.getCoupon() != null && o.getCoupon().getShop() != null
                        && Objects.equals(o.getCoupon().getShop().getShopId(), shopId)) {
                    // Ước lượng như trên: trước coupon
                    BigDecimal before = o.getItems().stream().map(it ->
                            nvl(it.getUnitPrice()).multiply(BigDecimal.valueOf(it.getQuantity()))
                                    .subtract(nvl(it.getDiscountAmount()))
                    ).reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (o.getCoupon().getDiscountType() == DiscountType.PERCENT) {
                        couponFee = before.multiply(nvl(o.getCoupon().getValue()).movePointLeft(2));
                    } else {
                        couponFee = nvl(o.getCoupon().getValue());
                    }
                    if (couponFee.compareTo(before) > 0) couponFee = before;
                }
                fees = fees.add(promoFee.add(couponFee));
            }
        }
        return gross.subtract(fees).subtract(returns);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
