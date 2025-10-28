package vn.host.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class BulkStatusReq {
    private List<Long> ids;
    private int status;
}
