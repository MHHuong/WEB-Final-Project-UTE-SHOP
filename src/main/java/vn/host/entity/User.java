package vn.host.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.host.util.sharedenum.UserRole;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 100, unique = true, nullable = false)
    @Email
    private String email;

    @Column(length = 255, nullable = true)
    private String passwordHash;

    @Column(length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('USER','SELLER','ADMIN','SHIPPER') default 'USER'")
    private UserRole role = UserRole.USER;

    @Column(columnDefinition = "tinyint default 1")
    private Integer status = 1;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "user")
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<Shop> shops = new HashSet<>();

    @OneToOne(mappedBy = "user")
    private Shipper shipper;
}