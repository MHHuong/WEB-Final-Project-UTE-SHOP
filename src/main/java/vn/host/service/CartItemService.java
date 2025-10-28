package vn.host.service;

import jakarta.transaction.Transactional;
import vn.host.entity.CartItem;
import vn.host.entity.OrderItem;
import vn.host.model.request.CartRequest;
import vn.host.model.response.CartResponse;
import vn.host.model.response.PageResponse;

import java.util.List;


public interface CartItemService {
    List<CartResponse> findUserCartItems(Long userId);

    PageResponse<CartResponse> findUserCartItemsPaginated(Long userId, int page, int size);

    void deleteById(Long id);

    void saveCart(CartRequest entity);

    @Transactional
    void removeCartItems(Long userId, List<OrderItem> orderItems);

    void updateCartItemQuantity(Long cartId, Integer quantity);
}
