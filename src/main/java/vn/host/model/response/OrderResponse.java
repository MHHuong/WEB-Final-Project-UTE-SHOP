package vn.host.model.response;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import vn.host.entity.*;
import vn.host.model.request.AddressRequest;
import vn.host.model.request.OrderItemRequest;
import vn.host.repository.OrderRepository;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;
import vn.host.util.sharedenum.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
  Long orderId;
  Long userId;
  Long shopId;
  PaymentMethod paymentMethod;
  BigDecimal totalAmount;
  OrderStatus status;
  AddressRequest address;
  Instant createdAt;
  PaymentStatus paymentStatus;
  BigDecimal couponValue;
  Integer estimatedDeliveryDays;
  List<OrderItemResponse> orderItem;

  public OrderResponse(Long orderId, Long userId, Long shopId, PaymentMethod paymentMethod, BigDecimal totalAmount, OrderStatus status, AddressRequest address, Instant createdAt, Integer estimatedDeliveryDays) {
      this.orderId = orderId;
      this.userId = userId;
      this.shopId = shopId;
      this.paymentMethod = paymentMethod;
      this.totalAmount = totalAmount;
      this.status = status;
      this.address = address;
      this.createdAt = createdAt;
      this.estimatedDeliveryDays = estimatedDeliveryDays;
  }
}
