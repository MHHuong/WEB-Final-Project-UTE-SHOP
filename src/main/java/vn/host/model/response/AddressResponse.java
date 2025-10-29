package vn.host.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long addressId;
    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private Integer isDefault;

    // Full address string for display
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", addressDetail, ward, district, province);
    }
}

