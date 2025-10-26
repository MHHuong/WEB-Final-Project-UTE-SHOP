package vn.host.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.entity.*;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    Long userId;
    PaymentMethod paymentMethod;
    Long shippingProviderId;
    BigDecimal totalAmount;
    Payment payments;
    String coupon;
    AddressRequest address;
    String receiverName;
    String phone;
    String note;
    List<OrderItemRequest> orders;
}
