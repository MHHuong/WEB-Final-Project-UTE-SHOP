package vn.host.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingFeeResponse {
    BigDecimal fee;
    Long shippingProviderId;
}
