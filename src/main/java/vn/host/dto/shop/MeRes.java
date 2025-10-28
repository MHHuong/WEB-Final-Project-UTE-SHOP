package vn.host.dto.shop;

import lombok.Data;

@Data
public class MeRes {
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private String phone;
    private ShopRes shop;
}
