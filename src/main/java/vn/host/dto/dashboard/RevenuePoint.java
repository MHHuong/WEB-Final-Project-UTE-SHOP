package vn.host.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenuePoint {
    private LocalDate date;

    private BigDecimal gross;

    private BigDecimal salesFee;

    private BigDecimal returns;

    private BigDecimal net;
}
