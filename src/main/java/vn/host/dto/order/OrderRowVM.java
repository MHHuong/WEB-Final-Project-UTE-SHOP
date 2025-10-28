package vn.host.dto.order;

import lombok.*;
import vn.host.entity.Order;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRowVM {
    private Long orderId;
    private String customerName;
    private String customerEmail;
    private Instant createdAt;
    private PaymentMethod paymentMethod;
    private String status;
    private BigDecimal amount;
    private boolean hasCoupon;

    public static OrderRowVM of(Order o) {
        return OrderRowVM.builder()
                .orderId(o.getOrderId())
                .customerName(o.getUser() != null ? o.getUser().getFullName() : null)
                .customerEmail(o.getUser() != null ? o.getUser().getEmail() : null)
                .createdAt(o.getCreatedAt())
                .paymentMethod(o.getPaymentMethod())
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .amount(o.getTotalAmount())
                .hasCoupon(o.getCoupon() != null)
                .build();
    }
}
