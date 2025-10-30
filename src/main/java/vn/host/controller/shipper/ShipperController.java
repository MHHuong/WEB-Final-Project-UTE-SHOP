package vn.host.controller.shipper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.shipper.RegisterShipperReq;
import vn.host.dto.shipper.ShipperOrderRowVM;
import vn.host.entity.*;
import vn.host.model.websocket.OrderStatusMessage;
import vn.host.service.OrderShipperLogService;
import vn.host.service.ShipperService;
import vn.host.service.ShippingProviderService;
import vn.host.service.UserService;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.ShipperAction;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final UserService users;
    private final ShippingProviderService providers;
    private final ShipperService shipperSvc;
    private final OrderShipperLogService logs;
    private final SimpMessagingTemplate messagingTemplate;

    private User authed(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        return users.getUserByEmail(auth.getName());
    }

    // 1) Đăng ký thành shipper (chỉ USER, không SELLER, mỗi account 1 shipper)
    @PostMapping("/register")
    public ResponseEntity<?> register(Authentication auth, @RequestBody RegisterShipperReq req) {
        User u = authed(auth);

        ShippingProvider sp = providers.findById(req.getShippingProviderId());

        Shipper sh = new Shipper();
        sh.setCompanyName(req.getCompanyName());
        sh.setPhone(req.getPhone());
        sh.setAddress(req.getAddress());
        sh.setShippingProvider(sp);

        Shipper saved = shipperSvc.registerAsShipper(u, sh);
        return ResponseEntity.ok(saved.getShipperId());
    }

    // Lấy profile shipper của user hiện tại
    @GetMapping("/me")
    public ResponseEntity<?> myProfile(Authentication auth) {
        User u = authed(auth);
        var opt = shipperSvc.findByUserId(u.getUserId());
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Not a shipper");
        return ResponseEntity.ok(vn.host.dto.shipper.ShipperProfileVM.of(opt.get()));
    }

    // Cập nhật profile shipper hiện tại
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(Authentication auth, @RequestBody RegisterShipperReq req) {
        User u = authed(auth);
        var opt = shipperSvc.findByUserId(u.getUserId());
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Not a shipper");
        Shipper me = opt.get();
        if (req.getCompanyName() != null) me.setCompanyName(req.getCompanyName());
        if (req.getPhone() != null) me.setPhone(req.getPhone());
        if (req.getAddress() != null) me.setAddress(req.getAddress());
        if (req.getShippingProviderId() != null) {
            var sp = providers.findById(req.getShippingProviderId());
            me.setShippingProvider(sp);
        }
        return ResponseEntity.ok(shipperSvc.edit(me));
    }

    // 3) Danh sách đơn CONFIRMED (đang chờ lấy) theo khu vực shipper
    @GetMapping("/orders/confirmed")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> confirmed(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));

        Sort s = parseSort(sort, "createdAt");
        Page<Order> pg = shipperSvc.listOrdersForShipper(me, OrderStatus.CONFIRMED, PageRequest.of(page, size, s));
        return ResponseEntity.ok(PageResult.of(pg.map(ShipperOrderRowVM::of)));
    }

    // 4) Danh sách đơn SHIPPING (đang giao) của shipper
    @GetMapping("/orders/shipping")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> shipping(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));

        Sort s = parseSort(sort, "createdAt");
        Page<Order> pg = shipperSvc.listOrdersForShipper(me, OrderStatus.SHIPPING, PageRequest.of(page, size, s));
        return ResponseEntity.ok(PageResult.of(pg.map(ShipperOrderRowVM::of)));
    }

    // 5) Nhận hàng (pickup): CONFIRMED -> SHIPPING và gán shipper
    @PostMapping("/orders/{orderId}/pickup")
    public ResponseEntity<?> pickup(Authentication auth, @PathVariable Long orderId) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        Order updated = shipperSvc.pickup(orderId, me);
        List<Long> receivers = new ArrayList<>();
        List<Shipper> shippers = updated.getShippingProvider().getShippers().stream().toList();
        for (Shipper shipper : shippers) {
            receivers.add(shipper.getUser().getUserId());
        }
        receivers.add(updated.getShop().getOwner().getUserId());
        receivers.add(updated.getUser().getUserId());
        for (Long receiverId : receivers) {
            OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, updated.getStatus().toString());
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
        }
        return ResponseEntity.ok(ShipperOrderRowVM.of(updated));
    }

    // 6) Giao thành công: SHIPPING -> DELIVERED (chỉ đơn được assign cho mình)
    @PostMapping("/orders/{orderId}/deliver")
    public ResponseEntity<?> deliver(Authentication auth, @PathVariable Long orderId) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        Order updated = shipperSvc.deliver(orderId, me);
        List<Long> receivers = new ArrayList<>();
        List<Shipper> shippers = updated.getShippingProvider().getShippers().stream().toList();
        for (Shipper shipper : shippers) {
            receivers.add(shipper.getUser().getUserId());
        }
        receivers.add(updated.getShop().getOwner().getUserId());
        receivers.add(updated.getUser().getUserId());
        for (Long receiverId : receivers) {
            OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, updated.getStatus().toString());
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
        }
        return ResponseEntity.ok(ShipperOrderRowVM.of(updated));
    }

    // ===== Helpers =====
    private Sort parseSort(String sort, String fallback) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.DESC, fallback);
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    @GetMapping("/history/picked")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> historyPicked(
            Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var pg = logs.findByShipper_ShipperIdAndAction(me.getShipperId(), ShipperAction.PICKUP,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(l -> ShipperOrderRowVM.of(l.getOrder()));
        return ResponseEntity.ok(PageResult.of(pg));
    }

    @GetMapping("/history/delivered")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> historyDelivered(
            Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var pg = logs.findByShipper_ShipperIdAndAction(me.getShipperId(), ShipperAction.DELIVER,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(l -> ShipperOrderRowVM.of(l.getOrder()));
        return ResponseEntity.ok(PageResult.of(pg));
    }

    @GetMapping("/orders/return/pickup")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> listReturnPickup(Authentication auth,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "createdAt,desc") String sort) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var pg = shipperSvc.listReturnPickup(me, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(ShipperOrderRowVM::of);
        return ResponseEntity.ok(PageResult.of(pg));
    }

    // --- RETURN: deliver list (đã RETURN_PICKUP nhưng chưa RETURN_DELIVER) ---
    @GetMapping("/orders/return/deliver")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> listReturnDeliver(Authentication auth,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(defaultValue = "createdAt,desc") String sort) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var pg = shipperSvc.listReturnDeliver(me, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(ShipperOrderRowVM::of);
        return ResponseEntity.ok(PageResult.of(pg));
    }

    // --- RETURN: hành động đánh dấu đã LẤY hàng trả ---
    @PostMapping("/orders/{orderId}/return/pickup")
    public ResponseEntity<Long> doReturnPickup(Authentication auth, @PathVariable Long orderId) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var saved = shipperSvc.returnPickup(orderId, me);
        return ResponseEntity.ok(saved.getOrderId());
    }

    // --- RETURN: hành động đánh dấu đã GIAO hàng trả về shop ---
    @PostMapping("/orders/{orderId}/return/deliver")
    public ResponseEntity<Long> doReturnDeliver(Authentication auth, @PathVariable Long orderId) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var saved = shipperSvc.returnDeliver(orderId, me);
        return ResponseEntity.ok(saved.getOrderId());
    }

    // --- History cho trả hàng (tương tự /history/picked | /history/delivered) ---
    @GetMapping("/history/return/pickup")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> historyReturnPickup(Authentication auth,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var pg = logs.findByShipper_ShipperIdAndAction(me.getShipperId(), ShipperAction.RETURN_PICKUP,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(l -> ShipperOrderRowVM.of(l.getOrder()));
        return ResponseEntity.ok(PageResult.of(pg));
    }

    @GetMapping("/history/return/deliver")
    public ResponseEntity<PageResult<ShipperOrderRowVM>> historyReturnDeliver(Authentication auth,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        var pg = logs.findByShipper_ShipperIdAndAction(me.getShipperId(), ShipperAction.RETURN_DELIVER,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(l -> ShipperOrderRowVM.of(l.getOrder()));
        return ResponseEntity.ok(PageResult.of(pg));
    }
}
