package vn.host.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.host.entity.CartItem;
import vn.host.entity.OrderItem;
import vn.host.entity.Product;
import vn.host.entity.User;
import vn.host.model.request.CartRequest;
import vn.host.model.response.CartResponse;
import vn.host.repository.CartItemRespository;
import vn.host.repository.ProductRespository;
import vn.host.repository.UserRepository;
import vn.host.service.CartItemService;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    CartItemRespository cartItemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRespository productRespository;

    @Override
    public List<CartResponse> findUserCartItems(Long userId) {
        return cartItemRepository.findCartItemsByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public  void saveCart(CartRequest entity) {
        User user = userRepository.findById(entity.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRespository.findById(entity.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> cartItem = cartItemRepository.findCartItemByProduct_ProductIdAndUser_UserId
                (entity.getProductId(), entity.getUserId());
        CartItem cart = null;
        if (cartItem.isPresent()) {
            int quantity = cartItem.get().getQuantity() + entity.getQuantity();
            cartItem.get().setQuantity((int) quantity);
        }
        else {
            cart = CartItem.builder()
            .product(product)
            .user(user)
            .quantity(entity.getQuantity())
            .build();
        }
        cartItemRepository.save(cartItem.orElse(cart));
    }

    @Override
    public void deleteByProductIdAndUserId(List<Long> productId, Long userId) {
        cartItemRepository.deleteByUser_UserIdAndProduct_ProductIdIn(userId, productId);
    }

    @Transactional
    @Override
    public void removeCartItems(Long userId, List<OrderItem> orderItems) {
        List<Long> productIds = orderItems.stream()
                .map(orderItem -> orderItem.getProduct().getProductId())
                .toList();
        cartItemRepository.deleteByUser_UserIdAndProduct_ProductIdIn(userId, productIds);
    }

    @Override
    public void updateCartItemQuantity(Long cartId, Integer quantity) {
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartId);
        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        } else {
            throw new RuntimeException("Cart item not found with id: " + cartId);
        }
    }
}
