package vn.host.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.util.sharedenum.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
