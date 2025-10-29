package vn.host.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopProductVM {
    private Long productId;
    private String name;
    private Long totalQty;
    private BigDecimal revenue;
}
