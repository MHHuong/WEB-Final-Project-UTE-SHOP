package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.host.dto.common.PageResult;
import vn.host.dto.promotion.PromotionVM;
import vn.host.entity.Category;
import vn.host.entity.Promotion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PromotionService {
    Page<Promotion> getAll(String keyword, Pageable pageable);

    Promotion findById(Long id);

    Promotion save(Map<String, Object> body);

    Promotion update(Long id, Map<String, Object> body);

    void delete(Long id);

    List<Promotion> getActivePromotions(Category category);

    String getStatus(Promotion promotion);

    void save(Promotion promotion);

    Promotion findById(long id);

    Page<Promotion> findByShop_ShopId(long shopId, Pageable pageable);

    boolean existsOverlappingGlobal(Long shopId, LocalDate startDate, LocalDate endDate, Long ignoreId);

    boolean existsOverlappingForCategory(Long shopId, Long categoryId, LocalDate startDate, LocalDate endDate, Long ignoreId);

    PageResult<PromotionVM> searchOwnerPromotions(String userEmail, String q, String status, int page, int size, Sort sort);
}
