package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.ShippingProvider;
import vn.host.repository.ShippingProviderRepository;
import vn.host.service.ShippingProviderService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingProviderServiceImpl implements ShippingProviderService {

    private final ShippingProviderRepository repo;

    @Override
    public Page<ShippingProvider> getAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.trim().isEmpty()) {
            return repo.findAll(pageable);
        }
        return repo.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    @Override
    public ShippingProvider findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping provider not found"));
    }

    @Override
    public ShippingProvider save(ShippingProvider provider) {
        validateProvider(provider);
        return repo.save(provider);
    }

    @Override
    public ShippingProvider update(Long id, ShippingProvider provider) {
        ShippingProvider existing = findById(id);
        existing.setName(provider.getName());
        existing.setFee(provider.getFee());
        existing.setEstimatedDays(provider.getEstimatedDays());
        validateProvider(existing);
        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private void validateProvider(ShippingProvider provider) {
        BigDecimal fee = provider.getFee();
        Integer days = provider.getEstimatedDays();

        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0 || fee.compareTo(BigDecimal.valueOf(1_000_000)) > 0) {
            throw new IllegalArgumentException("Shipping fee must be between 0 and 1,000,000 ₫!");
        }
        if (days == null || days < 1 || days > 14) {
            throw new IllegalArgumentException("Estimated delivery time must be between 1 and 14 days!");
        }
        if (provider.getName() == null || provider.getName().trim().length() < 3) {
            throw new IllegalArgumentException("Provider name must be at least 3 characters!");
        }
    }

    @Override
    public ShippingProvider findById(long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Shipping provider not found!"));
    }

    @Override
    public List<ShippingProvider> listAll() {
        return repo.findAllByOrderByNameAsc();
    }
}
