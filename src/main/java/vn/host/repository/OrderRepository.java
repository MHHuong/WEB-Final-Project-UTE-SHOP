package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Order;
import vn.host.util.sharedenum.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByUser_UserId(Long userId, Pageable pageable);
    Page<Order> findByShop_ShopId(Long shopId, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
}
