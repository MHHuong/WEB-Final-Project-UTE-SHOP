package vn.host.service;

import vn.host.entity.Shop;
import vn.host.entity.User;

import java.util.List;

public interface ShopService {
    void save(Shop shop);
    void delete(long id);
    List<Shop> findAll();
    Shop findById(long id);
    Shop getMyShopOrNull(Long userId);
    Shop registerOneShopForOwner(User owner, Shop incoming);
    Shop updateMyShop(Long ownerUserId, java.util.function.Consumer<Shop> mutator);
}
