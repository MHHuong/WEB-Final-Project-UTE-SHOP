package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.ShippingProvider;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider,Long> {
    @Query("""
        SELECT min(sp.fee)
        FROM ShippingProvider sp
        WHERE sp.estimatedDays <= :maxEstimatedDays
        """)
    Double findByMaxEstimatedDays(int maxEstimatedDays);
}
