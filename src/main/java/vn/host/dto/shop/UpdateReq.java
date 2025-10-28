package vn.host.dto.shop;

import lombok.Data;

@Data
public class UpdateReq {
    private String shopName;
    private String address;
    private String description;
    private String phone;
}
