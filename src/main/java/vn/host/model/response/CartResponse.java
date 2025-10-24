package vn.host.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.entity.Payment;
import vn.host.model.request.AddressRequest;
import vn.host.model.request.OrderItemRequest;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    public Long cartId;
    public Long userId;
    public Integer quantity;
    public ProductModel productModel;
}
