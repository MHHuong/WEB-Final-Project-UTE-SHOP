package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.host.util.sharedenum.ShipperAction;

import java.time.Instant;

@Entity
@Table(name = "order_shipper_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShipperLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK tới order
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    // FK tới shipper
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipper_id")
    private Shipper shipper;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShipperAction action;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void pre() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
