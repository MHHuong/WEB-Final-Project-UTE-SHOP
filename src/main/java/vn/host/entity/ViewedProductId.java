package vn.host.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ViewedProductId implements java.io.Serializable {
    private Long userId;
    private Long productId;
}