package vn.host.dto.shop;

import lombok.Data;

@Data
public class RegisterReq {
    private String shopName;
    private String address;
    private String description;
}
