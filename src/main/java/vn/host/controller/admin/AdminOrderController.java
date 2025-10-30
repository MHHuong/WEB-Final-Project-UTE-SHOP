package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.order.OrderReturnResponse;
import vn.host.entity.Order;
import vn.host.service.OrderService;
import vn.host.util.sharedenum.OrderStatus;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

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
        return ResponseEntity.ok("Đã duyệt yêu cầu trả hàng!");
    }

    // ✅ 3. Từ chối yêu cầu trả hàng
    @PutMapping("/{orderId}/reject-return")
    public ResponseEntity<?> rejectReturn(@PathVariable Long orderId) {
        orderService.updateStatusFast(orderId, OrderStatus.CANCELLED.name(), "Yêu cầu trả hàng bị từ chối");
        return ResponseEntity.ok("Đã từ chối yêu cầu trả hàng!");
    }

    // ✅ 4. Xác nhận đã nhận hàng hoàn tất
    @PutMapping("/{orderId}/confirm-returned")
    public ResponseEntity<?> confirmReturned(@PathVariable Long orderId) {
        orderService.updateStatusFast(orderId, OrderStatus.RETURNED.name(), null);
        return ResponseEntity.ok("Đơn hàng đã được trả lại thành công!");
    }
}
