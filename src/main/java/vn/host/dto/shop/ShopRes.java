package vn.host.dto.shop;

import lombok.Data;
import vn.host.util.BeanRead;

@Data
public class ShopRes {
    private Long shopId;
    private String shopName;
    private String address;
    private String description;
    private Boolean hasLogo;

    public static ShopRes of(vn.host.entity.Shop s) {
        ShopRes r = new ShopRes();
        r.shopId = s.getShopId();
        r.shopName = s.getShopName();
        r.address = s.getAddress();
        r.description = s.getDescription();
        try {
            Object logo = BeanRead.readField(s, "logo");
            r.hasLogo = (logo != null);
        } catch (Exception e) {
            r.hasLogo = null;
        }
        return r;
    }
}
