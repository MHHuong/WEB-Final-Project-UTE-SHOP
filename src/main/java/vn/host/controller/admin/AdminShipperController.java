package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Order;
import vn.host.entity.Shipper;
import vn.host.service.ShipperService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/shippers")
@RequiredArgsConstructor
public class AdminShipperController {

    private final ShipperService shipperService;

    @GetMapping
    public ResponseEntity<Page<Shipper>> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(shipperService.getAll(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipper> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shipperService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Shipper> create(@RequestBody Shipper shipper) {
        return ResponseEntity.ok(shipperService.save(shipper));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipper> update(
            @PathVariable Long id,
            @RequestBody Shipper shipper
    ) {
        return ResponseEntity.ok(shipperService.update(id, shipper));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        shipperService.delete(id);
        return ResponseEntity.ok("Deleted shipper ID = " + id);
    }

    @GetMapping("/orders/unassigned")
    public Page<Order> getUnassignedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return shipperService.getUnassignedOrders(page, size);
    }

    @PutMapping("/orders/{orderId}/assign")
    public ResponseEntity<?> assignOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Long> body
    ) {
        Long shipperId = body.get("shipperId");
        Order updated = shipperService.assignOrderToShipper(orderId, shipperId);
        return ResponseEntity.ok(Map.of(
                "message", "Order assigned successfully!",
                "orderId", updated.getOrderId(),
                "shipperId", shipperId
        ));
    }
}
