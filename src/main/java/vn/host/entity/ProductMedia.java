package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.host.util.sharedenum.MediaType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "productmedia")
public class ProductMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(length = 255, nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('image','video') default 'image'")
    private MediaType type = MediaType.image;
}