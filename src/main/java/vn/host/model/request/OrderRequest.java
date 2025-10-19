package vn.host.model.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.entity.*;
import vn.host.model.response.CartResponse;
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
    Long CouponId;
    AddressRequest address;
    String receiverName;
    String phone;
    List<OrderItemRequest> orders;
}
