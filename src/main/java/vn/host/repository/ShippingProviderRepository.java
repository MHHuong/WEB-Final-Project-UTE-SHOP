package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.ShippingProvider;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long>, JpaSpecificationExecutor<ShippingProvider> {
    Page<ShippingProvider> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
