package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShopId", nullable = false)
    @JsonIgnoreProperties({"products", "hibernateLazyInitializer", "handler"})
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId")
<<<<<<< HEAD
    @JsonIgnoreProperties({"products", "hibernateLazyInitializer", "handler"})
=======
    @JsonIgnore
>>>>>>> feature/payment
    private Category category;

    @Column(length = 200, nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    private Integer stock = 0;

    @Column(columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "product")
    @JsonIgnoreProperties("product")
    private Set<ProductMedia> media = new HashSet<>();

    @OneToMany(mappedBy = "product")
    private Set<Review> reviews = new HashSet<>();
}