package vn.host.controller.common;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.shipper.ShippingProviderVM;
import vn.host.service.ShippingProviderService;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-providers")
@RequiredArgsConstructor
public class ShippingProviderController {

    private final ShippingProviderService providers;

    // GET /api/shipping-providers  -> dùng cho form đăng ký shipper
    @GetMapping
    public ResponseEntity<List<ShippingProviderVM>> listAll() {
        var vms = providers.listAll().stream().map(ShippingProviderVM::of).toList();
        return ResponseEntity.ok(vms);
    }

    // (tuỳ chọn) GET /api/shipping-providers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ShippingProviderVM> getById(@PathVariable Long id) {
        var sp = providers.findById(id);
        return ResponseEntity.ok(ShippingProviderVM.of(sp));
    }
}
