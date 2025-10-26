package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.Shipper;
import vn.host.entity.ShippingProvider;
import vn.host.entity.User;
import vn.host.repository.ShipperRepository;
import vn.host.repository.ShippingProviderRepository;
import vn.host.repository.UserRepository;
import vn.host.service.ShipperService;
import vn.host.util.sharedenum.UserRole;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {

    private final ShipperRepository shipperRepository;
    private final UserRepository userRepository;
    private final ShippingProviderRepository shippingProviderRepository;

    @Override
    public Page<Shipper> getAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.isBlank()) {
            return shipperRepository.findAll(pageable);
        }
        return shipperRepository.findByUser_FullNameContainingIgnoreCaseOrShippingProvider_NameContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Override
    public Shipper findById(Long id) {
        return shipperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipper not found with ID: " + id));
    }

    @Override
    public Shipper save(Shipper shipper) {
        // Validate input
        if (shipper.getUser() == null || shipper.getUser().getUserId() == null)
            throw new RuntimeException("User information is required!");
        if (shipper.getShippingProvider() == null || shipper.getShippingProvider().getShippingProviderId() == null)
            throw new RuntimeException("Shipping provider information is required!");

        Long userId = shipper.getUser().getUserId();
        Long providerId = shipper.getShippingProvider().getShippingProviderId();

        // Validate user & provider
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        ShippingProvider provider = shippingProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Shipping provider not found with ID: " + providerId));

        // Validate user role
        if (user.getRole() != UserRole.SHIPPER)
            throw new RuntimeException("User must have role SHIPPER to be assigned as a shipper!");

        // Check duplicate shipper
        if (shipperRepository.existsByUser_UserId(userId))
            throw new RuntimeException("This user is already assigned as a shipper!");

        shipper.setUser(user);
        shipper.setShippingProvider(provider);

        return shipperRepository.save(shipper);
    }

    @Override
    public Shipper update(Long id, Shipper shipper) {
        Shipper existing = findById(id);

        // Không cho phép thay đổi user
        existing.setCompanyName(shipper.getCompanyName());
        existing.setPhone(shipper.getPhone());

        if (shipper.getShippingProvider() != null && shipper.getShippingProvider().getShippingProviderId() != null) {
            ShippingProvider provider = shippingProviderRepository.findById(
                    shipper.getShippingProvider().getShippingProviderId()
            ).orElseThrow(() -> new RuntimeException("Shipping provider not found!"));
            existing.setShippingProvider(provider);
        }

        return shipperRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Shipper existing = findById(id);
        if (existing.getAssignedOrders() != null && !existing.getAssignedOrders().isEmpty()) {
            throw new RuntimeException("Cannot delete shipper because they have assigned orders!");
        }
        shipperRepository.delete(existing);
    }
}
