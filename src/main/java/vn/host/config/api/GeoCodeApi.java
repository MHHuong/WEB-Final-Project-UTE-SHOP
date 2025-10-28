package vn.host.config.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hereGeocodeClient", url = "https://geocode.search.hereapi.com")
public interface GeoCodeApi {
    @GetMapping("/v1/geocode")
    Map<String, Object> getGeocode(
            @RequestParam("q") String address,
            @RequestParam("apiKey") String apiKey
    );
}
