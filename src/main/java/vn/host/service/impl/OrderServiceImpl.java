package vn.host.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.host.entity.*;
import vn.host.model.request.OrderItemRequest;
import vn.host.model.request.OrderRequest;
import vn.host.model.response.OrderResponse;
import vn.host.model.websocket.OrderStatusMessage;
import vn.host.repository.*;
import vn.host.service.AddressService;
import vn.host.service.CartItemService;
import vn.host.service.OrderService;
import vn.host.util.sharedenum.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

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
    ProductRespository productRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private CartItemService cartItemService;

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
        Coupon coupon = couponRepository.findById(orderRequest.getCouponId()).orElse(null);
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
                    .totalAmount(items.stream()
                            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .coupon(coupon)
                    .status(OrderStatus.NEW)
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
                        .build();
            }).collect(Collectors.toList());


            orderItemRepository.saveAll(orderItemEntities);
            cartItemService.removeCartItems(orderRequest.getUserId(), orderItemEntities);
            return savedOrder;
        }).toList();
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findAllOrdersByUserId(userId);
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(Enum.valueOf(vn.host.util.sharedenum.OrderStatus.class, status));
        orderRepository.save(order);
        // Publish per-user notification to /user/queue/orders
        Long userId = order.getUser().getUserId();
        OrderStatusMessage message = new OrderStatusMessage(order.getOrderId(), userId, order.getStatus().name());
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/orders", message);
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
}
