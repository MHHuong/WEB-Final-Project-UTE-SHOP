package vn.host.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(length = 100, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentId")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private Set<Category> children = new HashSet<>();

    @OneToMany(mappedBy = "category")
    @JsonIgnoreProperties({"category", "shop"})
    private Set<Product> products = new HashSet<>();
}