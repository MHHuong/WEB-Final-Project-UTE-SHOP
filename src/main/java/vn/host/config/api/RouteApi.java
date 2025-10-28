package vn.host.config.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hereRouteClient", url = "https://router.hereapi.com")
public interface RouteApi {
    @GetMapping(value = "/v8/routes", consumes = "application/json")
    Map<String, Object> getRoute(
            @RequestParam("transportMode") String transportMode,
            @RequestParam("origin") String source,
            @RequestParam("destination") String destination,
            @RequestParam("return") String returnType,
            @RequestParam("apiKey") String apiKey
    );
}
