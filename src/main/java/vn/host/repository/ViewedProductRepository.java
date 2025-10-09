package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.ViewedProduct;
import vn.host.entity.ViewedProductId;

@Repository
public interface ViewedProductRepository extends JpaRepository<ViewedProduct, ViewedProductId>, JpaSpecificationExecutor<ViewedProduct> {
    Page<ViewedProduct> findById_UserId(Long userId, Pageable pageable);
}
