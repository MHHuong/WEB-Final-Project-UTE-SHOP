package vn.host.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateReq {
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

    private List<Long> removeMediaIds;
}