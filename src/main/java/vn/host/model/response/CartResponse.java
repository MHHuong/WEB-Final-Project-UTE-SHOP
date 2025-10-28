package vn.host.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    public Long cartId;
    public Long userId;
    public Integer quantity;
    public ProductResponse productResponse;
}
