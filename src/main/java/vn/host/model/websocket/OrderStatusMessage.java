package vn.host.model.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusMessage {
    private Long orderId;
    private Long userId;
    private String status;
}

