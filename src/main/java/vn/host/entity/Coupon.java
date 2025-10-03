package vn.host.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.host.util.sharedenum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "coupons")
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(length = 50, unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @NotNull
    private LocalDateTime expiredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShopId")
    private Shop shop;

    @OneToMany(mappedBy = "coupon")
    private Set<Order> orders = new HashSet<>();
}