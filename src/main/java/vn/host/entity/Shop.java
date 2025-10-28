package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @JsonIgnore
    private User owner;

    @Column(length = 100, nullable = false)
    private String shopName;

    @Lob
    private String description;

    @Column(length = 255)
    private String logo;

    @Column(columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "shop")
    @JsonIgnoreProperties({"category", "shop"})
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "shop")
    @JsonIgnore
    private Set<Coupon> coupons = new HashSet<>();

    @OneToMany(mappedBy = "shop")
    @JsonIgnore
    private Set<Promotion> promotions = new HashSet<>();
}