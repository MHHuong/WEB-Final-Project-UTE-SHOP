package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.ShippingProvider;
import vn.host.model.response.ShippingFeeResponse;

import java.util.List;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider,Long> {
    @Query("""
    SELECT new vn.host.model.response.ShippingFeeResponse(
        sp.fee,
        sp.shippingProviderId
    )
    FROM ShippingProvider sp
    WHERE sp.estimatedDays <= :maxEstimatedDays
    ORDER BY sp.fee ASC
    """)
    List<ShippingFeeResponse> findTopByEstimatedDaysLessThanEqualOrderByFeeAsc(int maxEstimatedDays);
}
