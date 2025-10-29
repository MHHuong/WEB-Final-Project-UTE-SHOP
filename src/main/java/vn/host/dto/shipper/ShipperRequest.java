package vn.host.dto.shipper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import vn.host.entity.ShippingProvider;
import vn.host.entity.User;

@Data
public class ShipperRequest {
    @JsonProperty("user")
    private User user;

    @JsonProperty("shippingProvider")
    private ShippingProvider shippingProvider;

    private String companyName;
    private String phone;
}
