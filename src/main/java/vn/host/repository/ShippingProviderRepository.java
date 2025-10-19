package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.host.entity.ShippingProvider;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider,Long> {
}
