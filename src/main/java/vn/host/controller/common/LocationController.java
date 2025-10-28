package vn.host.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.location.LocationVM;
import vn.host.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<LocationVM.SimpleItem>> provinces() {
        return ResponseEntity.ok(service.listProvinces());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<LocationVM.SimpleItem>> districts(@RequestParam Integer provinceCode) {
        return ResponseEntity.ok(service.listDistrictsByProvince(provinceCode));
    }

    @GetMapping("/wards")
    public ResponseEntity<List<LocationVM.SimpleItem>> wards(@RequestParam Integer districtCode) {
        return ResponseEntity.ok(service.listWardsByDistrict(districtCode));
    }
}