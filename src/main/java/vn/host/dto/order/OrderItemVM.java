package vn.host.dto.order;

import lombok.*;
import vn.host.entity.OrderItem;
import vn.host.entity.Product;
import vn.host.entity.ProductMedia;
import vn.host.util.sharedenum.MediaType;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemVM {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String thumbUrl;
    private Integer quantity;
    private BigDecimal unitPrice;       // giá gốc / đơn vị
    private BigDecimal itemDiscount;    // giảm theo item (promotion)
    private BigDecimal couponShare;     // phần giảm do coupon phân bổ
    private BigDecimal lineOriginal;    // unitPrice * qty
    private BigDecimal lineFinal;       // sau giảm

    public static OrderItemVM of(OrderItem it, BigDecimal couponShare) {
        Product p = it.getProduct();

        // Lấy thumbnail từ Product.media (Set<ProductMedia>)
        String thumb = null;
        if (p != null && p.getMedia() != null && !p.getMedia().isEmpty()) {
            thumb = p.getMedia().stream()
                    .filter(m -> m.getType() == MediaType.image)
                    .map(ProductMedia::getUrl)
                    .findFirst()
                    .orElse(null);
        }

        BigDecimal u = it.getUnitPrice() != null ? it.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal q = BigDecimal.valueOf(it.getQuantity() == null ? 0 : it.getQuantity());
        BigDecimal lineOriginal = u.multiply(q);
        BigDecimal itemDisc = it.getDiscountAmount() == null ? BigDecimal.ZERO : it.getDiscountAmount();
        BigDecimal couponPart = couponShare == null ? BigDecimal.ZERO : couponShare;

        BigDecimal lineFinal = lineOriginal.subtract(itemDisc).subtract(couponPart);
        if (lineFinal.signum() < 0) lineFinal = BigDecimal.ZERO;

        return OrderItemVM.builder()
                .orderItemId(it.getOrderItemId())
                .productId(p != null ? p.getProductId() : null)
                .productName(p != null ? p.getName() : null)
                .thumbUrl(thumb)
                .quantity(it.getQuantity())
                .unitPrice(u)
                .itemDiscount(itemDisc)
                .couponShare(couponPart)
                .lineOriginal(lineOriginal)
                .lineFinal(lineFinal)
                .build();
    }
}
