package vn.host.service;

import vn.host.entity.Shop;

import java.util.List;

public interface ShopService {
    void save(Shop shop);
    void delete(long id);
    List<Shop> findAll();
    Shop findById(long id);
}
