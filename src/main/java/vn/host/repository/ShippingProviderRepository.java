package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.ShippingProvider;
import vn.host.model.response.ShippingFeeResponse;

import java.util.List;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, Long>, JpaSpecificationExecutor<ShippingProvider> {
    Page<ShippingProvider> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<ShippingProvider> findAllByOrderByNameAsc();

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
