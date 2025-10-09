package vn.host.service;

import vn.host.entity.ShippingProvider;

import java.util.List;

public interface ShippingProviderService {
    void save(ShippingProvider shippingProvider);
    void delete(long id);
    List<ShippingProvider> findAll();
    ShippingProvider findById(long id);
}
