package vn.host.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import vn.host.entity.Payment;
import vn.host.entity.Shop;
import vn.host.model.request.AddressRequest;
import vn.host.model.request.OrderItemRequest;
import vn.host.repository.OrderRepository;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
  Long userId;
  Long shopId;
  PaymentMethod paymentMethod;
  BigDecimal totalAmount;
  OrderStatus status;
  AddressRequest address;
  Instant createdAt;
}
