package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.order.OrderReturnResponse;
import vn.host.entity.Order;
import vn.host.entity.Shipper;
import vn.host.model.websocket.OrderStatusMessage;
import vn.host.service.OrderService;
import vn.host.util.sharedenum.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    // ✅ 1. Lấy tất cả đơn có trạng thái liên quan trả hàng
    @GetMapping("/returns")
    public Page<OrderReturnResponse> getAllReturnOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.findAllReturnOrdersDto(pageable);
    }

    // ✅ 2. Duyệt yêu cầu trả hàng
    @PutMapping("/{orderId}/approve-return")
    public ResponseEntity<?> approveReturn(@PathVariable Long orderId) {
        orderService.updateStatusFast(orderId, OrderStatus.RETURNING.name(), null);
        Order updated = orderService.findById(orderId);
        List<Long> receivers = new ArrayList<>();
        receivers.add(updated.getShop().getOwner().getUserId());
        receivers.add(updated.getUser().getUserId());
        for (Long receiverId : receivers) {
            OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, updated.getStatus().toString());
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
        }
        return ResponseEntity.ok("Đã duyệt yêu cầu trả hàng!");
    }

    // ✅ 3. Từ chối yêu cầu trả hàng
    @PutMapping("/{orderId}/reject-return")
    public ResponseEntity<?> rejectReturn(@PathVariable Long orderId) {
        orderService.updateStatusFast(orderId, OrderStatus.CANCELLED.name(), "Yêu cầu trả hàng bị từ chối");
        Order updated = orderService.findById(orderId);
        List<Long> receivers = new ArrayList<>();
        receivers.add(updated.getUser().getUserId());
        for (Long receiverId : receivers) {
            OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, updated.getStatus().toString());
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
        }
        return ResponseEntity.ok("Đã từ chối yêu cầu trả hàng!");
    }

    // ✅ 4. Xác nhận đã nhận hàng hoàn tất
    @PutMapping("/{orderId}/confirm-returned")
    public ResponseEntity<?> confirmReturned(@PathVariable Long orderId) {
        orderService.updateStatusFast(orderId, OrderStatus.RETURNED.name(), null);
        return ResponseEntity.ok("Đơn hàng đã được trả lại thành công!");
    }

    // ✅ 5. Tìm kiếm
    @GetMapping("/returns/search")
    public Page<OrderReturnResponse> searchReturnOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.searchReturnOrdersByCustomer(keyword, pageable);
    }
}
