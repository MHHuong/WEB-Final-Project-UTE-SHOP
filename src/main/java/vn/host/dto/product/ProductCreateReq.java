package vn.host.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateReq {
    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    private Integer stock;
    private Integer status;

    private String description;
}