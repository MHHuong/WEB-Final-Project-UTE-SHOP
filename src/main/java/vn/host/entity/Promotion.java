package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "promotions")
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ShopId")
    private Shop shop;

    @Column(length = 100, nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApplyCategoryId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category applyCategory;
}