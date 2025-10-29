package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.host.util.sharedenum.MediaType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity @Table(name = "ReviewMedia")
public class ReviewMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewMediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReviewId", nullable = false)
    private Review review;

    @Column(length = 255, nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('image','video') default 'image'")
    private MediaType type = MediaType.image;
}