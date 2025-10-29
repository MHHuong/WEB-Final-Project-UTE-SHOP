package vn.host.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.ShippingProvider;
import vn.host.service.ShippingProviderService;

@RestController
@RequestMapping("/api/admin/shipping-providers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminShippingProviderController {

    private final ShippingProviderService service;

    @GetMapping
    public ResponseEntity<Page<ShippingProvider>> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getAll(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingProvider> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ShippingProvider> create(@RequestBody ShippingProvider provider) {
        return ResponseEntity.ok(service.save(provider));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingProvider> update(
            @PathVariable Long id,
            @RequestBody ShippingProvider provider
    ) {
        return ResponseEntity.ok(service.update(id, provider));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Deleted provider id=" + id);
    }
}
