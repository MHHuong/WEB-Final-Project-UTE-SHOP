package vn.host.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.host.config.api.GeoCodeApi;
import vn.host.config.api.RouteApi;
import vn.host.dto.order.OrderReturnResponse;
import vn.host.entity.*;
import vn.host.model.request.OrderItemRequest;
import vn.host.model.request.OrderRequest;
import vn.host.model.request.ShippingFeeRequest;
import vn.host.model.response.*;
import vn.host.model.websocket.OrderStatusMessage;
import vn.host.repository.*;
import vn.host.service.AddressService;
import vn.host.service.CartItemService;
import vn.host.service.EmailService;
import vn.host.service.OrderService;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.PaymentMethod;
import vn.host.util.sharedenum.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private GeoCodeApi geoCodeApi;

    @Autowired
    private RouteApi routeConfig;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressService addressService;

    @Autowired
    ShippingProviderRepository shippingProviderRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${here.api.key}")
    private String hereApiKey;
    @Autowired
    private EmailService emailService;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void saveOrder(OrderRequest orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ShippingProvider shippingProvider = shippingProviderRepository.findById(orderRequest.getShippingProviderId())
                .orElseThrow(() -> new RuntimeException("Shipping Provider not found"));
        Coupon coupon = couponRepository.findByCode(orderRequest.getCoupon()).orElse(null);
        Address address = addressService.findAddressByAddressDetail(orderRequest.getAddress(), user);

        List<OrderItemRequest> orderItems = orderRequest.getOrders();
        Map<Long, List<OrderItemRequest>> itemsByShop = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemRequest::getShopId));

        List<Order> orders = itemsByShop.entrySet().stream().map(entry -> {
            Long shopId = entry.getKey();
            List<OrderItemRequest> items = entry.getValue();

            Order order = Order.builder()
                    .user(user)
                    .shop(shopRepository.findById(shopId)
                            .orElseThrow(() -> new RuntimeException("Shop not found")))
                    .address(address)
                    .paymentMethod(orderRequest.getPaymentMethod())
                    .shippingProvider(shippingProvider)
                    .totalAmount(orderRequest.getTotalAmount())
                    .coupon(coupon)
                    .status(OrderStatus.NEW)
                    .note(orderRequest.getNote())
                    .build();

            Order savedOrder = orderRepository.save(order);

            List<OrderItem> orderItemEntities = items.stream().map(item -> {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);

                return OrderItem.builder()
                        .order(savedOrder)
                        .product(product)
                        .unitPrice(item.getPrice())
                        .quantity(item.getQuantity())
                        .discountAmount(item.getDiscountAmount())
                        .build();
            }).collect(Collectors.toList());
            orderItemRepository.saveAll(orderItemEntities);
            cartItemService.removeCartItems(orderRequest.getUserId(), orderItemEntities);
            return savedOrder;
        }).toList();
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<OrderResponse> orders = orderRepository.findAllOrdersByUserId(userId);
        for (OrderResponse order : orders) {
            List<OrderItemResponse> items = orderItemRepository.findOrderByOrderIdAndUserId(order.getOrderId(), userId);
            order.setOrderItem(items);
            Order current_order = orderRepository.findById(order.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            if (current_order.getCoupon() != null) {
                order.setCouponValue(current_order.getCoupon().getValue());
            }
            Optional<Payment> payment = current_order.getPayments()
                    .stream()
                    .findFirst();
            if (payment.isPresent()) {
                order.setPaymentStatus(payment.get().getStatus());
            } else {
                order.setPaymentStatus(PaymentStatus.PENDING);
            }
        }
        return orders;
    }

    @Override
    public OrderResponse getOrderByOrderId(Long orderId) {
        OrderResponse order = orderRepository.findOrderById(orderId);
        List<OrderItemResponse> items = orderItemRepository.findOrderByOrderIdAndUserId(order.getOrderId(), order.getUserId());
        for (OrderItemResponse item : items) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            product.ifPresent(value -> item.setImage(value.getMedia().stream().findFirst().map(ProductMedia::getUrl).orElse(null)));
        }
        Order current_order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (current_order.getCoupon() != null) {
            order.setCouponValue(current_order.getCoupon().getValue());
        }
        Optional<Payment> payment = current_order.getPayments().stream().findFirst();
        if (payment.isPresent()) {
            order.setPaymentStatus(payment.get().getStatus());
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        order.setOrderItem(items);
        return order;
    }

    @Override
    public void updateStatus(Long orderId, String status, String reason, String bankAccountInfo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if ((order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED && OrderStatus.valueOf(status) == OrderStatus.CANCELLED)) {
            throw new RuntimeException("Only NEW orders can be cancelled");
        }
        order.setStatus(Enum.valueOf(vn.host.util.sharedenum.OrderStatus.class, status));
        if (order.getStatus().equals(OrderStatus.RECEIVED)) {
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .method(order.getPaymentMethod())
                    .status(PaymentStatus.SUCCESS)
                    .transactionCode("AUTO-" + order.getOrderId() + "-" + System.currentTimeMillis())
                    .build();
            order.getPayments().add(paymentRepository.save(payment));
        }


        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = order.getNote() != null ? order.getNote() : "";
            String newNotes = currentNotes + "\n[" + status + "] Lý do: " + reason
                    + "\n[Tài khoản ngân hàng]" + bankAccountInfo;
            order.setNote(newNotes.trim());
        }
        orderRepository.save(order);

        Long userId = order.getUser().getUserId();
        OrderStatusMessage message = new OrderStatusMessage(order.getOrderId(), userId, order.getStatus().name());
        Long shopId = order.getShop().getOwner().getUserId();
        OrderStatusMessage messageShop = new OrderStatusMessage(order.getOrderId(), shopId, order.getStatus().name());
        List<Shipper> shippers = order.getShippingProvider().getShippers().stream().toList();
        for (Shipper shipper : shippers) {
            OrderStatusMessage messageShipper = new OrderStatusMessage(order.getOrderId(), shipper.getUser().getUserId(), order.getStatus().name());
            messagingTemplate.convertAndSendToUser(String.valueOf(shipper.getUser().getUserId()), "/queue/orders", messageShipper);
        }
        System.out.println("📤 Sending WebSocket message:");
        System.out.println("   → To userId: " + userId);
        System.out.println("   → Order ID: " + order.getOrderId());
        System.out.println("   → Status: " + order.getStatus().name());
        System.out.println("   → Destination: /user/" + userId + "/queue/orders");

        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/orders", message);
        messagingTemplate.convertAndSendToUser(String.valueOf(shopId), "/queue/orders", messageShop);

        System.out.println("✅ WebSocket message sent!");

        emailService.sendOrderStatusEmail(order, order.getUser(), order.getStatus());
    }

    @Override
    public void updatePayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.getPayments().add(payment);
        orderRepository.save(order);
    }

    @Override
    public Order findOrderById(long l) {
        return orderRepository.findById(l)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private ShippingFeeResponse calDistanceFee(String shippingService) {
        int estimatedDays = 0;
        switch (shippingService) {
            case "STANDARD":
                estimatedDays = 5;
                break;
            case "FAST":
                estimatedDays = 2;
                break;
            case "EXPRESS":
                estimatedDays = 1;
                break;
            default:
                estimatedDays = 5;
        }
        List<ShippingFeeResponse> shippingProviders = shippingProviderRepository.findTopByEstimatedDaysLessThanEqualOrderByFeeAsc(estimatedDays);
        if (shippingProviders.isEmpty()) {
            throw new RuntimeException("No shipping providers found for the given service");
        }
        return shippingProviders.get(0);
    }

    @Override
    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest shippingFeeRequest) {
        try {
            ShippingFeeResponse distanceFee = calDistanceFee(shippingFeeRequest.getShippingService());
            double sum = 0;
            String source = shippingFeeRequest.getWard() + ", " + shippingFeeRequest.getDistrict() + ", " + shippingFeeRequest.getProvince();
            Map<String, Object> originData = geoCodeApi.getGeocode(source, hereApiKey);
            for (Long id : shippingFeeRequest.getShopIds()) {
                Shop shop = shopRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Shop not found"));
                Address shopAddress = shop.getOwner().getAddresses().stream().findFirst().orElse(null);
                if (shopAddress == null) {
                    throw new RuntimeException("Shop address not found");
                }
                String destination = shopAddress.getWard() + ", " + shopAddress.getDistrict() + ", " + shopAddress.getProvince();
                Map<String, Object> destData = geoCodeApi.getGeocode(destination, hereApiKey);

                Map<String, Object> originPos = (Map<String, Object>) ((List<?>) originData.get("items")).get(0);
                Map<String, Object> destPos = (Map<String, Object>) ((List<?>) destData.get("items")).get(0);

                Map<String, Double> originMap = (Map<String, Double>) originPos.get("position");
                Map<String, Double> destMap = (Map<String, Double>) destPos.get("position");

                String origin = originMap.get("lat") + "," + originMap.get("lng");
                String dest = destMap.get("lat") + "," + destMap.get("lng");


                Map<String, Object> routeData = routeConfig.getRoute("car", origin, dest, "summary", hereApiKey);
                List<Map<String, Object>> routes = (List<Map<String, Object>>) routeData.get("routes");
                Map<String, Object> firstRoute = routes.get(0);
                List<Map<String, Object>> sections = (List<Map<String, Object>>) firstRoute.get("sections");
                Map<String, Object> firstSection = sections.get(0);
                Map<String, Object> summary = (Map<String, Object>) firstSection.get("summary");
                sum += ((Number) summary.get("length")).doubleValue() / 100000;
            }
            distanceFee.setFee(BigDecimal.valueOf(sum * distanceFee.getFee().doubleValue()));
            return distanceFee;
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate shipping fee: " + e.getMessage());
        }
    }

    @Override
    public String findTopOrderByUser(TempOrderResponse tempOrderResponse) {
        List<ProductResponse> products = tempOrderResponse.getOrders();
        Long userId = tempOrderResponse.getUserId();
        List<Long> shopIds = products.stream()
                .map(ProductResponse::getShopId)
                .distinct()
                .toList();
        return shopIds.stream().map(shopId -> {
                    Order order = orderRepository.findTopByUser_UserIdAndShop_ShopIdOrderByOrderIdDesc(userId, shopId);
                    return order != null ? String.valueOf(order.getOrderId()) : "";
                }).filter(id -> !id.isEmpty())
                .collect(Collectors.joining("-"));
    }

    @Override
    public void updateOrderPaymentVnPay(String orderIdsStr, String responseCode, String transNo, Long amount) {
        String[] orderIds = orderIdsStr.split("-");
        for (String idStr : orderIds) {
            Long orderId = Long.parseLong(idStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            Payment payment = Payment.builder()
                    .order(order)
                    .transactionCode(transNo)
                    .amount(BigDecimal.valueOf(amount))
                    .method(PaymentMethod.VNPAY)
                    .build();
            if ("00".equals(responseCode)) {
                payment.setStatus(vn.host.util.sharedenum.PaymentStatus.SUCCESS);
                order.setStatus(OrderStatus.CONFIRMED);
            } else {
                payment.setStatus(vn.host.util.sharedenum.PaymentStatus.FAILED);
                order.setStatus(OrderStatus.CANCELLED);
            }
            Payment new_payment = paymentRepository.save(payment);
            order.getPayments().add(new_payment);
            orderRepository.save(order);
        }
    }

    @Override
    public void updateOrderPaymentMomo(String orderIdsStr, Integer responseCode, String transNo, Long amount) {
        String[] orderIds = orderIdsStr.split("-");
        for (String idStr : orderIds) {
            Long orderId = Long.parseLong(idStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            Payment payment = Payment.builder()
                    .order(order)
                    .transactionCode(transNo)
                    .amount(BigDecimal.valueOf(amount))
                    .method(PaymentMethod.MOMO)
                    .build();
            if (responseCode == 0) {
                payment.setStatus(vn.host.util.sharedenum.PaymentStatus.SUCCESS);
            } else {
                payment.setStatus(vn.host.util.sharedenum.PaymentStatus.FAILED);
                order.setStatus(OrderStatus.CANCELLED);
            }
            Payment new_payment = paymentRepository.save(payment);
            order.getPayments().add(new_payment);
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
    }

    @Override
    public Page<Order> findByShop_ShopId(Long shopId, Pageable pageable) {
        return orderRepository.findByShop_ShopId(shopId, pageable);
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Override
    public Page<Order> findAll(Specification<Order> spec, Pageable pageable) {
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public Page<OrderReturnResponse> findAllReturnOrdersDto(Pageable pageable) {
        List<OrderStatus> statuses = List.of(
                OrderStatus.REQUEST_RETURN,
                OrderStatus.RETURNING,
                OrderStatus.RETURNED,
                OrderStatus.CANCELLED
        );

        Page<Order> orders = orderRepository.findByStatusIn(statuses, pageable);

        return orders.map(order -> OrderReturnResponse.builder()
                .orderId(order.getOrderId())
                .shopName(order.getShop() != null ? order.getShop().getShopName() : null)
                .userName(order.getUser() != null ? order.getUser().getFullName() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .createdAt(order.getCreatedAt() != null
                        ? order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .note(order.getNote())
                .build());
    }

    @Override
    @Transactional
    public void updateStatusFast(Long orderId, String newStatus, String note) {
        try {
            OrderStatus status = OrderStatus.valueOf(newStatus);
            orderRepository.updateOrderStatusFast(orderId, status, note);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật trạng thái nhanh: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<OrderReturnResponse> searchReturnOrdersByCustomer(String keyword, Pageable pageable) {
        Page<Order> orders;

        if (keyword == null || keyword.trim().isEmpty()) {
            orders = orderRepository.findAllReturnOrders(pageable);
        } else {
            orders = orderRepository.findByUser_FullNameContainingIgnoreCaseAndStatusIn(
                    keyword,
                    List.of(OrderStatus.REQUEST_RETURN, OrderStatus.RETURNING, OrderStatus.RETURNED, OrderStatus.CANCELLED),
                    pageable
            );
        }

        return orders.map(OrderReturnResponse::fromEntity);
    }
}
