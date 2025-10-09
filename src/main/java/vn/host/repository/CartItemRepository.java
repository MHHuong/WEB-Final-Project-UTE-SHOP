package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long>, JpaSpecificationExecutor<CartItem> {
    Optional<CartItem> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
    List<CartItem> findByUser_UserId(Long userId);
    void deleteByUser_UserId(Long userId);
}
