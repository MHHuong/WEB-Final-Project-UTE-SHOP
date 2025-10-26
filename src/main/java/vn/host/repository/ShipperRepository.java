package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Shipper;

import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper,Long>, JpaSpecificationExecutor<Shipper> {
    Page<Shipper> findByUser_FullNameContainingIgnoreCaseOrShippingProvider_NameContainingIgnoreCase(String name, String provider, Pageable pageable);
    boolean existsByUser_UserId(Long userId);
}
