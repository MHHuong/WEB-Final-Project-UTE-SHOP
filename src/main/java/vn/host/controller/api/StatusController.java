package vn.host.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.host.model.websocket.OrderStatusMessage;
import vn.host.service.OrderService;

@RestController
@RequestMapping("/api/status/ws")
@RequiredArgsConstructor
public class StatusController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderService orderService;

    @PostMapping("/send")
    public ResponseEntity<?> sendToUser(@RequestParam("userId") String userId,
                                        @RequestParam(value = "message", required = false, defaultValue = "TEST") String message) {
        OrderStatusMessage payload = new OrderStatusMessage(null, userId.matches("\\d+") ? Long.valueOf(userId) : null, message);
        messagingTemplate.convertAndSendToUser(userId, "/queue/orders", payload);
        return ResponseEntity.ok().body("sent to user " + userId);
    }

    @PostMapping("/order-status")
    public ResponseEntity<?> updateOrderStatus(@RequestParam("orderId") Long orderId,
                                               @RequestParam("status") String status) {
        orderService.updateStatus(orderId, status, null);
        return ResponseEntity.ok().body("order status updated");
    }
}

