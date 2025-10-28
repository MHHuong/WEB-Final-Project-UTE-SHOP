package vn.host.model.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    Long ProductId;
    String ProductName;
    int quantity;
    BigDecimal unitPrice;
    BigDecimal discountAmount;
    String image;

    public OrderItemResponse(Long ProductId, String productName, int quantity, BigDecimal unitPrice, BigDecimal discountAmount) {
        this.ProductId = ProductId;
        ProductName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountAmount = discountAmount;
    }

    public OrderItemResponse(String productName, int quantity, BigDecimal unitPrice) {
        ProductName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}
