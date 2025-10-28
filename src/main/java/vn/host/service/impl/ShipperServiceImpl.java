package vn.host.service.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.host.entity.Order;
import vn.host.entity.OrderShipperLog;
import vn.host.entity.Shipper;
import vn.host.entity.User;
import vn.host.repository.OrderRepository;
import vn.host.repository.OrderShipperLogRepository;
import vn.host.repository.ShipperRepository;
import vn.host.repository.UserRepository;
import vn.host.service.ShipperService;
import vn.host.util.sharedenum.OrderStatus;
import vn.host.util.sharedenum.ShipperAction;
import vn.host.util.sharedenum.UserRole;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {
    private final ShipperRepository shipperRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderShipperLogRepository orderShipperLogRepository;

    @Override
    public void save(Shipper shipper) {
        shipperRepository.save(shipper);
    }

    @Override
    public void delete(long id) {
        shipperRepository.deleteById(id);
    }

    @Override
    public List<Shipper> findAll() {
        return shipperRepository.findAll();
    }

    @Override
    public Shipper findById(long id) {
        return shipperRepository.findById(id).orElse(null);
    }

    @Override
    public List<Shipper> findByShippingProviderId(long shippingProviderId) {
        return shipperRepository.findByShippingProvider_ShippingProviderId(shippingProviderId, Pageable.unpaged()).getContent();
    }

    private record Area(String province, String district) {
    }

    private Area resolveAreaFromAddress(String address) {
        if (address == null) return new Area(null, null);
        String[] parts = java.util.Arrays.stream(address.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (parts.length < 2) {
            return new Area(null, null);
        }
        String provinceName = parts[parts.length - 1];
        String districtName = parts[parts.length - 2];

        return new Area(provinceName, districtName);
    }

    @Override
    public Optional<Shipper> findByUserId(Long userId) {
        return shipperRepository.findByUser_UserId(userId);
    }

    @Override
    public Shipper registerAsShipper(User user, Shipper newInfo) {
        if (user.getRole() == UserRole.SELLER) {
            throw new SecurityException("Shop owner cannot register as shipper");
        }

        if (shipperRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new IllegalStateException("This account already registered as a shipper");
        }

        newInfo.setUser(user);
        Shipper saved = shipperRepository.save(newInfo);

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.SHIPPER);
            userRepository.save(user);
        }
        return saved;
    }

    @Override
    public Page<Order> listOrdersForShipper(Shipper me, OrderStatus status, Pageable pageable) {
        Area area = resolveAreaFromAddress(me.getAddress());
        String province = area.province();
        String district = area.district();

        Specification<Order> spec = (root, cq, cb) -> {
            var ps = new java.util.ArrayList<Predicate>();
            ps.add(cb.equal(root.get("status"), status));
            if (status == OrderStatus.CONFIRMED) {
                ps.add(cb.isNull(root.get("shipper")));
            } else {
                ps.add(cb.equal(root.get("shipper"), me));
            }
            Join<Object, Object> addr = root.join("address", JoinType.INNER);

            if (StringUtils.hasText(province)) {
                ps.add(cb.equal(addr.get("province"), province));
            }
            if (StringUtils.hasText(district)) {
                ps.add(cb.equal(addr.get("district"), district));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public Order pickup(Long orderId, Shipper me) {
        Order o = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (o.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order not in CONFIRMED state");
        }
        Area area = resolveAreaFromAddress(me.getAddress());
        String province = area.province();
        String district = area.district();

        if (StringUtils.hasText(province) && StringUtils.hasText(district)) {
            // Phải đúng khu vực, so sánh đúng text (y như option text bên shop)
            if (!province.equals(o.getAddress().getProvince()) || !district.equals(o.getAddress().getDistrict())) {
                throw new SecurityException("Order outside your service area");
            }
        }

        o.setStatus(OrderStatus.SHIPPING);
        o.setShipper(me);
        o.addShipper(me);
        Order saved = orderRepository.save(o);
        orderShipperLogRepository.save(OrderShipperLog.builder().order(saved).shipper(me).action(ShipperAction.PICKUP).build());
        return saved;
    }

    @Override
    public Order deliver(Long orderId, Shipper me) {
        Order o = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (o.getStatus() != OrderStatus.SHIPPING) throw new IllegalStateException("Order not in SHIPPING state");
        if (o.getShipper() == null || !o.getShipper().getShipperId().equals(me.getShipperId())) {
            throw new SecurityException("You are not assigned to this order");
        }

        o.setStatus(OrderStatus.DELIVERED);
        o.setShipper(me);
        Order saved = orderRepository.save(o);

        orderShipperLogRepository.save(OrderShipperLog.builder()
                .order(saved).shipper(me).action(ShipperAction.DELIVER).build());

        return saved;
    }
}
