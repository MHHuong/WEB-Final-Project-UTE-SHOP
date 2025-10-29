package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @JsonIgnore
    private User user;

    @Column(length = 50)
    private String province;

    @Column(length = 50)
    private String district;

    @Column(length = 50)
    private String ward;

    @Column(length = 255)
    private String addressDetail;

    @Column(length = 100)
    private String receiverName;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @Column(columnDefinition = "tinyint default 0")
    private Integer isDefault = 0;
}
