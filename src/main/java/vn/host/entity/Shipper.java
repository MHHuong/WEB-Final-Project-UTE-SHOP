package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "shipper", "passwordHash", "addresses", "shops"})
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
    @JsonIgnore
    private Set<Order> assignedOrders = new HashSet<>();

    @ManyToMany(mappedBy = "shippers")
    @JsonIgnore
    private Set<Order> orders = new LinkedHashSet<>();
}