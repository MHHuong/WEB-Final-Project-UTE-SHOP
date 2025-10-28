package vn.host.dto.order;

import lombok.*;
import vn.host.entity.Coupon;
import vn.host.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVM {
    private Long orderId;
    private String status;
    private Instant createdAt;
    private String paymentMethod;
    private String customerName;
    private String customerEmail;
    private String receiverPhone;
    private String receiverName;
    private String shippingAddress; // gộp từ Address

    // Chỉ còn flag & code để hiển thị
    private boolean hasCoupon;
    private String couponCode;

    // Tổng hợp để render giá gốc & giá sau giảm
    private BigDecimal subtotalOriginal;   // sum(unitPrice*qty)
    private BigDecimal promotionDiscount;  // tổng giảm từ item (promotion)
    private BigDecimal couponDiscount;     // tổng giảm do coupon (toàn đơn)
    private BigDecimal shippingFee;        // nếu có
    private BigDecimal totalAmount;        // thành tiền cuối

    private List<OrderItemVM> items;

    public static OrderDetailVM of(
            Order o,
            List<OrderItemVM> items,
            BigDecimal subtotal,
            BigDecimal promoDisc,
            BigDecimal couponDisc,
            BigDecimal shipping
    ) {
        Coupon c = o.getCoupon();

        // Address dùng đúng field Address của bạn (ví dụ: addressDetail, ward, district, province)
        String addressStr = null;
        String phone = null;
        String name = null;
        if (o.getAddress() != null) {
            String detail = o.getAddress().getAddressDetail();
            String ward = o.getAddress().getWard();
            String district = o.getAddress().getDistrict();
            String province = o.getAddress().getProvince();
            phone = o.getAddress().getPhone();
            name = o.getAddress().getReceiverName();

            StringBuilder sb = new StringBuilder();
            if (detail != null && !detail.isBlank()) sb.append(detail);
            if (ward != null && !ward.isBlank()) sb.append(!sb.isEmpty() ? ", " : "").append(ward);
            if (district != null && !district.isBlank()) sb.append(!sb.isEmpty() ? ", " : "").append(district);
            if (province != null && !province.isBlank()) sb.append(!sb.isEmpty() ? ", " : "").append(province);
            addressStr = sb.isEmpty() ? null : sb.toString();
        }

        return OrderDetailVM.builder()
                .orderId(o.getOrderId())
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .createdAt(o.getCreatedAt())
                .paymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null)
                .customerName(o.getUser() != null ? o.getUser().getFullName() : null)
                .customerEmail(o.getUser() != null ? o.getUser().getEmail() : null)
                .receiverPhone(phone)
                .receiverName(name)
                .shippingAddress(addressStr)
                .hasCoupon(c != null)
                .couponCode(c != null ? c.getCode() : null)
                .subtotalOriginal(subtotal != null ? subtotal : BigDecimal.ZERO)
                .promotionDiscount(promoDisc != null ? promoDisc : BigDecimal.ZERO)
                .couponDiscount(couponDisc != null ? couponDisc : BigDecimal.ZERO)
                .shippingFee(shipping != null ? shipping : BigDecimal.ZERO)
                .totalAmount(o.getTotalAmount())
                .items(items)
                .build();
    }
}
