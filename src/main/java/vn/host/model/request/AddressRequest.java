package vn.host.model.request;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.host.entity.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    String province;
    String district;
    String ward;
    String addressDetail;
    String receiverName;
    String phone;
}
