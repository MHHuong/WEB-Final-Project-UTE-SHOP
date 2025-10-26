package vn.host.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrderItemRequest {
    Long productId;
    BigDecimal price;
    Integer quantity;
    Long shopId;
    BigDecimal discountAmount;
}
