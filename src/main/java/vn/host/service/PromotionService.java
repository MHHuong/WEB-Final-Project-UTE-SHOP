package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Promotion;

import java.time.LocalDate;
import java.util.List;

public interface PromotionService {
    void save(Promotion promotion);

    void delete(Long id);

    Promotion findById(long id);

    Page<Promotion> findByShop_ShopId(long shopId, Pageable pageable);

    boolean existsOverlappingGlobal(Long shopId, LocalDate startDate, LocalDate endDate, Long ignoreId);

    boolean existsOverlappingForCategory(Long shopId, Long categoryId, LocalDate startDate, LocalDate endDate, Long ignoreId);
}
