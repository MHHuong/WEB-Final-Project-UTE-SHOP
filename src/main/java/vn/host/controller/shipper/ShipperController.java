package vn.host.controller.shipper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.shipper.RegisterShipperReq;
import vn.host.dto.shipper.ShipperOrderRowVM;
import vn.host.entity.*;
import vn.host.service.OrderShipperLogService;
import vn.host.service.ShipperService;
import vn.host.service.ShippingProviderService;
import vn.host.service.UserService;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.ShipperAction;

@RestController
@RequestMapping("/api/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final UserService users;
    private final ShippingProviderService providers;
    private final ShipperService shipperSvc; // dùng impl vì có extra methods
    private final OrderShipperLogService logs;

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

    // 2) Lấy thông tin shipper hiện tại
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElse(null);
        if (me == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(me);
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
        return ResponseEntity.ok(ShipperOrderRowVM.of(updated));
    }

    // 6) Giao thành công: SHIPPING -> DELIVERED (chỉ đơn được assign cho mình)
    @PostMapping("/orders/{orderId}/deliver")
    public ResponseEntity<?> deliver(Authentication auth, @PathVariable Long orderId) {
        User u = authed(auth);
        Shipper me = shipperSvc.findByUserId(u.getUserId()).orElseThrow(() -> new SecurityException("Not a shipper"));
        Order updated = shipperSvc.deliver(orderId, me);
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
}
