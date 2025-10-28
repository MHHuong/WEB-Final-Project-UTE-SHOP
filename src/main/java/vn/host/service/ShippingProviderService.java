package vn.host.service;

import vn.host.entity.ShippingProvider;

import java.util.List;

public interface ShippingProviderService {
    ShippingProvider findById(long id);

    List<ShippingProvider> listAll();
}
