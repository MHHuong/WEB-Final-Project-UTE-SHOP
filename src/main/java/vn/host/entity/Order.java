package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "addresses", "shops", "shipper"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShopId", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products", "owner"})
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressId", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
    private Address address;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippingProviderId", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "shippers"})
    private ShippingProvider shippingProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShipperId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orders", "user"})
    private Shipper shipper;

    @ManyToMany
    @JoinTable(
            name = "orders_shippers",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "shipper_id")
    )
    private Set<Shipper> shippers = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CouponId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orders"})
    private Coupon coupon;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "order", orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<Payment> payments = new HashSet<>();

    public void addShipper(Shipper s) {
        if (s == null) return;
        if (shippers == null) shippers = new LinkedHashSet<>();
        shippers.add(s);
    }
}