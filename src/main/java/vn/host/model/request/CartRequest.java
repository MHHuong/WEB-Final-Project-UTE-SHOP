package vn.host.model.request;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import vn.host.entity.Product;
import vn.host.entity.User;
import vn.host.model.response.ProductModel;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequest {
    public Long userId;
    public Long productId;
    public Integer quantity;
}
