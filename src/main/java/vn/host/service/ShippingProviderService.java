package vn.host.service;

import org.springframework.data.domain.Page;
import vn.host.entity.ShippingProvider;

import java.util.List;

public interface ShippingProviderService {
    Page<ShippingProvider> getAll(String keyword, int page, int size);

    ShippingProvider findById(Long id);

    ShippingProvider save(ShippingProvider provider);

    ShippingProvider update(Long id, ShippingProvider provider);

    void delete(Long id);

    ShippingProvider findById(long id);

    List<ShippingProvider> listAll();
}
