package vn.host.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.entity.Order;
import vn.host.util.sharedenum.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReturnResponse {
    private Long orderId;
    private String shopName;
    private String userName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private String note;

    public static OrderReturnResponse fromEntity(Order order) {
        if (order == null) return null;

        OrderStatus status = null;
        if (order.getStatus() != null) {
            status = order.getStatus();
        }

        String paymentMethod = null;
        if (order.getPaymentMethod() != null) {
            paymentMethod = order.getPaymentMethod().name();
        }

        return OrderReturnResponse.builder()
                .orderId(order.getOrderId())
                .shopName(order.getShop() != null ? order.getShop().getShopName() : null)
                .userName(order.getUser() != null ? order.getUser().getFullName() : null)
                .totalAmount(order.getTotalAmount())
                .status(status)  // ✅ Giữ nguyên Enum
                .paymentMethod(paymentMethod)
                .createdAt(order.getCreatedAt() != null
                        ? order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .note(order.getNote())
                .build();
    }
}
