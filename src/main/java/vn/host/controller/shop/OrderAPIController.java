package vn.host.controller.shop;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.order.OrderDetailVM;
import vn.host.dto.order.OrderItemVM;
import vn.host.dto.order.OrderRowVM;
import vn.host.dto.order.UpdateStatusReq;
import vn.host.entity.*;
import vn.host.model.websocket.OrderStatusMessage;
import vn.host.service.*;
import vn.host.util.sharedenum.DiscountType;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.ShipperAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shop/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class OrderAPIController {

    private final UserService userService;
    private final ShopService shopService;
    private final OrderService orderService;
    private final ProductService productService;
    private final OrderShipperLogService orderShipperLogService;
    private final SimpMessagingTemplate messagingTemplate;

    private User authedUser(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        return userService.getUserByEmail(auth.getName());
    }

    private Shop myShopOr403(User u) {
        Shop s = shopService.getMyShopOrNull(u.getUserId());
        if (s == null) throw new SecurityException("Shop not registered");
        return s;
    }

    // ===== LIST (phân trang + search + filter) =====
    @GetMapping
    public ResponseEntity<PageResult<OrderRowVM>> list(Authentication auth,
                                                       @RequestParam(required = false) String q,
                                                       @RequestParam(required = false) OrderStatus status,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(defaultValue = "createdAt,desc") String sort) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Sort sortObj = parseSort(sort, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<Order> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("shop").get("shopId"), s.getShopId()));
            if (status != null) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(q)) {
                String kw = "%" + q.trim().toLowerCase() + "%";
                var userJoin = root.join("user", jakarta.persistence.criteria.JoinType.LEFT);
                List<Predicate> or = new ArrayList<>();
                or.add(cb.like(cb.lower(userJoin.get("fullName")), kw));
                or.add(cb.like(cb.lower(userJoin.get("email")), kw));
                // nếu có code/phone/address trong entity, có thể nối thêm ở đây
                ps.add(cb.or(or.toArray(new Predicate[0])));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Order> pageData = orderService.findAll(spec, pageable);
        List<OrderRowVM> rows = pageData.map(OrderRowVM::of).getContent();

        PageResult<OrderRowVM> result = PageResult.<OrderRowVM>builder()
                .content(rows)
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();

        return ResponseEntity.ok(result);
    }

    // ===== DETAIL (order-single.html) =====
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailVM> detail(Authentication auth, @PathVariable Long orderId) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Order o = orderService.findById(orderId);

        // chặn truy cập chéo shop
        if (o.getShop() == null || !o.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }

        // === TÍNH TOÁN breakdown ===
        BigDecimal subtotalOriginal = BigDecimal.ZERO; // sum(qty * unitPrice)
        BigDecimal promoDiscount = BigDecimal.ZERO;    // sum(item.discountAmount)
        BigDecimal afterPromoSum = BigDecimal.ZERO;    // subtotalOriginal - promoDiscount

        for (OrderItem it : o.getItems()) {
            BigDecimal qty = BigDecimal.valueOf(it.getQuantity());
            BigDecimal unit = nz(it.getUnitPrice());
            BigDecimal lineGross = unit.multiply(qty);
            BigDecimal itemDisc = nz(it.getDiscountAmount()); // đã bao gồm promotion (shop/admin)
            BigDecimal lineAfterPromo = lineGross.subtract(itemDisc).max(BigDecimal.ZERO);

            subtotalOriginal = subtotalOriginal.add(lineGross);
            promoDiscount = promoDiscount.add(itemDisc);
            afterPromoSum = afterPromoSum.add(lineAfterPromo);
        }

        // coupon discount: dựa theo loại coupon (nếu có) trên phần sau-promotion
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Coupon c = o.getCoupon();
        if (c != null && c.getValue() != null && c.getValue().compareTo(BigDecimal.ZERO) > 0) {
            if (c.getDiscountType() == DiscountType.PERCENT) {
                couponDiscount = afterPromoSum.multiply(c.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else { // AMOUNT
                couponDiscount = c.getValue().min(afterPromoSum);
            }
        }

        // shippingFee: suy ra nếu entity có field, tốt nhất là gọi getter; nếu null, suy từ total
        BigDecimal shippingFee = BigDecimal.ZERO;
        try {
            var m = Order.class.getMethod("getShippingFee");
            Object v = m.invoke(o);
            if (v instanceof BigDecimal bd) shippingFee = nz(bd);
        } catch (Exception ignored) {
            // Suy luận: total = afterPromoSum - coupon + shipping
            shippingFee = nz(o.getTotalAmount()).subtract(afterPromoSum.subtract(couponDiscount));
            if (shippingFee.compareTo(BigDecimal.ZERO) < 0) shippingFee = BigDecimal.ZERO;
        }

        // Phân bổ coupon cho từng item theo tỷ trọng afterPromo
        List<OrderItemVM> itemVMs = new ArrayList<>();
        for (OrderItem it : o.getItems()) {
            BigDecimal qty = BigDecimal.valueOf(it.getQuantity());
            BigDecimal unit = nz(it.getUnitPrice());
            BigDecimal lineGross = unit.multiply(qty);
            BigDecimal itemDisc = nz(it.getDiscountAmount());
            BigDecimal lineAfterPromo = lineGross.subtract(itemDisc).max(BigDecimal.ZERO);

            BigDecimal share = BigDecimal.ZERO;
            if (couponDiscount.compareTo(BigDecimal.ZERO) > 0 && afterPromoSum.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = lineAfterPromo.divide(afterPromoSum, 8, RoundingMode.HALF_UP);
                share = couponDiscount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            }
            itemVMs.add(OrderItemVM.of(it, share));
        }

        OrderDetailVM vm = OrderDetailVM.of(
                o,                      // order
                itemVMs,                // items
                subtotalOriginal,       // subtotalOriginal
                promoDiscount,          // promotionDiscount
                couponDiscount,         // couponDiscount
                shippingFee             // shippingFee
        );

        return ResponseEntity.ok(vm);
    }

    // ===== UPDATE STATUS (NEW -> CONFIRMED/CANCELLED) =====
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(Authentication auth,
                                             @PathVariable Long orderId,
                                             @RequestBody UpdateStatusReq req) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Order o = orderService.findById(orderId);
        if (o.getShop() == null || !o.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }

        if (o.getStatus() != OrderStatus.NEW) {
            return ResponseEntity.noContent().build(); // không đổi gì nếu không phải NEW
        }
        if (!StringUtils.hasText(req.getStatus())) {
            throw new IllegalArgumentException("status is required");
        }
        OrderStatus next = OrderStatus.valueOf(req.getStatus().toUpperCase());
        if (next != OrderStatus.CONFIRMED && next != OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Only CONFIRMED or CANCELLED is allowed for NEW orders");
        }

        if (next == OrderStatus.CONFIRMED) {
            for (OrderItem it : o.getItems()) {
                Product p = it.getProduct();
                if (p == null) continue;

                Integer current = p.getStock() == null ? 0 : p.getStock();

                // MẶC ĐỊNH: giảm theo số lượng đặt
                int qty = (it.getQuantity() == null ? 1 : it.getQuantity());

                // NẾU BẠN MUỐN "mỗi Confirm chỉ trừ đúng 1":
                // int qty = 1;

                int decreased = Math.max(0, current - qty);
                p.setStock(decreased);

                // Quy ước: 0 = In Stock, 1 = Out of Stock (theo yêu cầu "0 -> 1")
                if (decreased == 0) {
                    p.setStatus(1);
                }
                productService.save(p);
            }
            List<Long> receivers = new ArrayList<>();
            List<Shipper> shippers = o.getShippingProvider().getShippers().stream().toList();
            for (Shipper shipper : shippers) {
                receivers.add(shipper.getUser().getUserId());
            }
            receivers.add(o.getUser().getUserId());
            for (Long receiverId : receivers) {
                OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, next.toString());
                messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
            }
        } else {
            List<Long> receivers = new ArrayList<>();
            receivers.add(o.getUser().getUserId());
            for (Long receiverId : receivers) {
                OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, next.toString());
                messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
            }
        }
        o.setStatus(next);
        orderService.save(o);
        return ResponseEntity.noContent().build();
    }

    // ===== Helpers =====
    private Sort parseSort(String sort, String fallbackField) {
        if (!StringUtils.hasText(sort)) return Sort.by(Sort.Direction.DESC, fallbackField);
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @PostMapping("/{orderId}/return/approve")
    @Transactional
    public ResponseEntity<Void> approveReturn(Authentication auth, @PathVariable Long orderId) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Order o = orderService.findById(orderId);
        if (o.getShop() == null || !o.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }
        if (o.getStatus() != OrderStatus.REQUEST_RETURN) {
            return ResponseEntity.badRequest().build();
        }
        o.setStatus(OrderStatus.RETURNING);
        orderService.save(o);
        return ResponseEntity.ok().build();
    }

    /**
     * Shop TỪ CHỐI yêu cầu trả hàng:
     * REQUEST_RETURN -> DELIVERED
     */
    @PostMapping("/{orderId}/return/reject")
    @Transactional
    public ResponseEntity<Void> rejectReturn(Authentication auth, @PathVariable Long orderId) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Order o = orderService.findById(orderId);
        if (o.getShop() == null || !o.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }
        if (o.getStatus() != OrderStatus.REQUEST_RETURN) {
            return ResponseEntity.badRequest().build();
        }
        o.setStatus(OrderStatus.DELIVERED);
        orderService.save(o);
        return ResponseEntity.ok().build();
    }

    /**
     * Shop XÁC NHẬN ĐÃ NHẬN HÀNG TRẢ:
     * chỉ cho phép khi đang RETURNING và đã có log RETURN_DELIVER
     * RETURNING -> RETURNED
     */
    @PostMapping("/{orderId}/return/confirm-received")
    @Transactional
    public ResponseEntity<Void> confirmReceivedReturn(Authentication auth, @PathVariable Long orderId) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Order o = orderService.findById(orderId);
        if (o.getShop() == null || !o.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }
        if (o.getStatus() != OrderStatus.RETURNING) {
            return ResponseEntity.badRequest().build();
        }
        boolean deliveredBack = orderShipperLogService.existsAction(orderId, ShipperAction.RETURN_DELIVER);
        if (!deliveredBack) {
            // chưa có shipper trả hàng về shop -> không cho confirm
            return ResponseEntity.status(409).build(); // 409 Conflict
        }
        o.setStatus(OrderStatus.RETURNED);
        orderService.save(o);
        List<Long> receivers = new ArrayList<>();
        receivers.add(o.getShipper().getUser().getUserId());
        receivers.add(o.getUser().getUserId());
        for (Long receiverId : receivers) {
            OrderStatusMessage payload = new OrderStatusMessage(null, receiverId.toString().matches("\\d+") ? receiverId : null, o.getStatus().toString());
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/orders", payload);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * (Optional, để UI hiển thị nút “CONFIRM RECEIVED…” đúng lúc)
     * Kiểm tra có thể confirm chưa (dựa trên RETURN_DELIVER)
     */
    @GetMapping("/{orderId}/return/confirmable")
    public ResponseEntity<Boolean> confirmable(Authentication auth, @PathVariable Long orderId) {
        User u = authedUser(auth);
        Shop s = myShopOr403(u);

        Order o = orderService.findById(orderId);
        if (o.getShop() == null || !o.getShop().getShopId().equals(s.getShopId())) {
            return ResponseEntity.notFound().build();
        }
        boolean ok = (o.getStatus() == OrderStatus.RETURNING)
                && orderShipperLogService.existsAction(orderId, ShipperAction.RETURN_DELIVER);
        return ResponseEntity.ok(ok);
    }
}
