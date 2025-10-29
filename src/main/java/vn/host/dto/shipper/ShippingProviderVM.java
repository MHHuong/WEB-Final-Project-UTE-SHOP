package vn.host.dto.shipper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.host.entity.ShippingProvider;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingProviderVM {
    private Long shippingProviderId;
    private String name;

    public static ShippingProviderVM of(ShippingProvider sp) {
        return new ShippingProviderVM(sp.getShippingProviderId(), sp.getName());
    }
}
