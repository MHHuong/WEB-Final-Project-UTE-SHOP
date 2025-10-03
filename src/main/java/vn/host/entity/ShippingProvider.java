package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shippingproviders")
public class ShippingProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shippingProviderId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    private Integer estimatedDays = 3;

    @OneToMany(mappedBy = "shippingProvider")
    private Set<Shipper> shippers = new HashSet<>();
}