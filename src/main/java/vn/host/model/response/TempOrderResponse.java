package vn.host.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.entity.Order;
import vn.host.entity.Payment;
import vn.host.model.request.AddressRequest;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TempOrderResponse {
    Long userId;
    String orderCode;
    List<ProductModel> orders;
}
