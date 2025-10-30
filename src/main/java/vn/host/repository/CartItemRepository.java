package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.CartItem;
import vn.host.model.request.CartRequest;
import vn.host.model.response.CartResponse;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("""
    SELECT new vn.host.model.response.CartResponse(
        c.cartId,
        u.userId,
        c.quantity,
        new vn.host.model.response.ProductResponse(
            p.productId,
            s.shopId,
            s.shopName,
            p.name,
            p.price,
            MIN(m.url)
        )
    )
    FROM CartItem c
    JOIN c.user u
    JOIN c.product p
    JOIN p.shop s
    JOIN p.media m
    WHERE u.userId = :userId AND m.type = "IMAGE"
    GROUP BY u.userId, c.quantity, p.productId, s.shopId, s.shopName, p.name, p.price, c.cartId
""")
    List<CartResponse> findCartItemsByUserId(Long userId);

    Optional<CartItem> findCartItemByProduct_ProductIdAndUser_UserId(Long productId, Long userId);

    void deleteByUser_UserIdAndProduct_ProductIdIn(Long userId, List<Long> productIds);
}