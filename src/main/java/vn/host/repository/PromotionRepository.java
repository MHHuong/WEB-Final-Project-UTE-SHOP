package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Promotion;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion,Long>, JpaSpecificationExecutor<Promotion> {
    List<Promotion> findByShopIsNull();
}
