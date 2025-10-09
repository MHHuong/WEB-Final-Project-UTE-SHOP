package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop,Long>, JpaSpecificationExecutor<Shop> {
    Page<Shop> findByOwner_UserId(Long userId, Pageable pageable);
}
