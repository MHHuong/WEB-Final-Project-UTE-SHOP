package vn.host.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.ProductMedia;

import java.util.List;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long>, JpaSpecificationExecutor<ProductMedia> {
    List<ProductMedia> findByProduct_ProductId(Long productId);
}
