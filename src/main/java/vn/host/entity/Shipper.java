package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shippers")
public class Shipper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipperId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(length = 100)
    private String companyName;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippingProviderId", nullable = false)
    private ShippingProvider shippingProvider;

    @OneToMany(mappedBy = "shipper")
    private Set<Order> assignedOrders = new HashSet<>();

    @ManyToMany(mappedBy = "shippers")
    private Set<Order> orders = new LinkedHashSet<>();
}