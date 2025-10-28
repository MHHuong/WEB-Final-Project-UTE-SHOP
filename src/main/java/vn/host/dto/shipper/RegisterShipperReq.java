package vn.host.dto.shipper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterShipperReq {
    private Long shippingProviderId;
    private String companyName;
    private String phone;
    private String address;
    private String province;
    private String district;
}
