package vn.host.service;

import vn.host.entity.CartItem;

import java.util.List;

public interface CartItemService {
    void save(CartItem cartItem);
    void delete(long id);
    List<CartItem> findAll();
    CartItem findById(long id);
}
