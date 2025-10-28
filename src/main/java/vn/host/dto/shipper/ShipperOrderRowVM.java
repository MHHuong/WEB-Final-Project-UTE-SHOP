package vn.host.dto.shipper;

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
public class ShipperOrderRowVM {
    private Long orderId;
    private String productName;
    private String status;
    private String shippingProvider;
    private String shopName;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal amountForCOD;
    private Instant createdAt;

    public static ShipperOrderRowVM of(Order o) {
        String name = o.getItems() != null && !o.getItems().isEmpty()
                ? o.getItems().iterator().next().getProduct().getName()
                : "(No item)";
        if (o.getItems() != null && o.getItems().size() > 1) {
            name = name + " (+" + (o.getItems().size() - 1) + " items)";
        }
        BigDecimal visible = o.getPaymentMethod() == PaymentMethod.COD ? o.getTotalAmount() : BigDecimal.ZERO;

        String addressStr = String.join(", ",
                java.util.Arrays.asList(
                        o.getAddress().getAddressDetail(), o.getAddress().getWard(), o.getAddress().getDistrict(), o.getAddress().getProvince()
                ).stream().filter(s -> s != null && !s.isBlank()).toList()
        );

        return ShipperOrderRowVM.builder()
                .orderId(o.getOrderId())
                .productName(name)
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .shippingProvider(o.getShippingProvider() != null ? o.getShippingProvider().getName() : null)
                .shopName(o.getShop() != null ? o.getShop().getShopName() : null)
                .receiverName(o.getAddress().getReceiverName())
                .receiverPhone(o.getAddress().getPhone())
                .receiverAddress(addressStr)
                .amountForCOD(visible)
                .createdAt(o.getCreatedAt())
                .build();
    }
}
