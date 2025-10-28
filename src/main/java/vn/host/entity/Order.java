package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShopId", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressId", nullable = false)
    private Address address;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippingProviderId", nullable = false)
    private ShippingProvider shippingProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShipperId")
    private Shipper shipper;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CouponId")
    private Coupon coupon;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "order", orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<Payment> payments = new HashSet<>();
}