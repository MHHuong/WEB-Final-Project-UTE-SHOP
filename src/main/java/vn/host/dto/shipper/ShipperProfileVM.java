package vn.host.dto.shipper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.host.entity.Shipper;
import vn.host.entity.User;
import vn.host.entity.ShippingProvider;

@Getter
@Setter
@Builder
public class ShipperProfileVM {
    private Long shipperId;
    private String fullName;
    private String email;
    private String companyName;
    private String phone;
    private String address;
    private Long shippingProviderId;
    private String shippingProviderName;

    public static ShipperProfileVM of(Shipper s) {
        if (s == null) return null;

        User u = s.getUser();
        ShippingProvider sp = s.getShippingProvider();

        return ShipperProfileVM.builder()
                .shipperId(s.getShipperId())
                .fullName(u != null ? u.getFullName() : null)
                .email(u != null ? u.getEmail() : null)
                .companyName(s.getCompanyName())
                .phone(s.getPhone())
                .address(s.getAddress())
                .shippingProviderId(sp != null ? sp.getShippingProviderId() : null)
                .shippingProviderName(sp != null ? sp.getName() : null)
                .build();
    }
}
