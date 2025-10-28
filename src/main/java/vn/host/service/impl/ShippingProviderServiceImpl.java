package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.ShippingProvider;
import vn.host.repository.ShippingProviderRepository;
import vn.host.service.ShippingProviderService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingProviderServiceImpl implements ShippingProviderService {
    private final ShippingProviderRepository shippingProviderRepository;

    @Override
    public ShippingProvider findById(long id) {
        return shippingProviderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Shipping provider not found!"));
    }

    @Override
    public List<ShippingProvider> listAll() {
        return shippingProviderRepository.findAllByOrderByNameAsc();
    }
}
