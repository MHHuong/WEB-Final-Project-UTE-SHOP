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

import java.util.ArrayList;
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

    private String tailLike(String district, String province) {
        if (!org.springframework.util.StringUtils.hasText(district)
                || !org.springframework.util.StringUtils.hasText(province)) return null;
        return "%, " + district.trim() + ", " + province.trim();
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
        Area a = resolveAreaFromAddress(me.getAddress());
        String province = a.province(), district = a.district();

        Specification<Order> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), status));

            if (status == OrderStatus.CONFIRMED) {
                ps.add(cb.isNull(root.get("shipper")));

                Join<Object, Object> shop = root.join("shop", JoinType.INNER);
                String likeTail = tailLike(district, province);
                if (likeTail != null) {
                    ps.add(cb.like(shop.get("address"), likeTail));
                }
            } else if (status == OrderStatus.SHIPPING) {
                Join<Object, Object> recvAddr = root.join("address", JoinType.INNER);
                if (org.springframework.util.StringUtils.hasText(province)) {
                    ps.add(cb.equal(recvAddr.get("province"), province));
                }
                if (org.springframework.util.StringUtils.hasText(district)) {
                    ps.add(cb.equal(recvAddr.get("district"), district));
                }
                // Mặc định: chỉ thấy đơn SHIPPING của chính mình
                //ps.add(cb.equal(root.get("shipper"), me));
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
        Area a = resolveAreaFromAddress(me.getAddress());
        String province = a.province(), district = a.district();

        // Lấy địa chỉ SHOP
        var shop = o.getShop();
        var sa = (shop != null) ? shop.getAddress() : null;
        Area ashop = resolveAreaFromAddress(sa);
        String shopProvince = ashop.province(), shopDistrict = ashop.district();

        if (org.springframework.util.StringUtils.hasText(province) && org.springframework.util.StringUtils.hasText(district)) {
            if (!province.equals(shopProvince) || !district.equals(shopDistrict)) {
                throw new SecurityException("Order outside your pickup area (shop)");
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

        Area a = resolveAreaFromAddress(me.getAddress());
        String province = a.province(), district = a.district();

        var oa = o.getAddress(); // ManyToOne Address
        String recvProvince = (oa != null) ? oa.getProvince() : null;
        String recvDistrict = (oa != null) ? oa.getDistrict() : null;

        if (org.springframework.util.StringUtils.hasText(province) && org.springframework.util.StringUtils.hasText(district)) {
            if (!province.equals(recvProvince) || !district.equals(recvDistrict)) {
                throw new SecurityException("Order outside your delivery area (receiver)");
            }
        }

        o.setStatus(OrderStatus.DELIVERED);
        o.setShipper(me);
        Order saved = orderRepository.save(o);

        orderShipperLogRepository.save(OrderShipperLog.builder()
                .order(saved).shipper(me).action(ShipperAction.DELIVER).build());

        return saved;
    }
}
