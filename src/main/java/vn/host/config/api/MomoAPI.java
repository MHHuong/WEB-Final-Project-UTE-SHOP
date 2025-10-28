package vn.host.config.api;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.host.model.request.CreateMomoRequest;
import vn.host.model.response.CreateMomoResponse;


@FeignClient(name = "momo", url = "${momo.endpoint}")
public interface MomoAPI {
    @PostMapping("/create")
    CreateMomoResponse createMomoQR(@RequestBody CreateMomoRequest request);
}