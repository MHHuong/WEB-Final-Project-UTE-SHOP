package vn.host.dto.shop;

import lombok.Data;
import vn.host.entity.Shop;

@Data
public class ShopRes {
    private Long shopId;
    private String shopName;
    private String address;
    private String description;
    private String logo;

    public static ShopRes of(Shop s) {
        if (s == null) return null;
        ShopRes r = new ShopRes();
        r.setShopId(s.getShopId());
        r.setShopName(s.getShopName());
        r.setAddress(s.getAddress());
        r.setDescription(s.getDescription());
        r.setLogo(s.getLogo());
        return r;
    }
}
