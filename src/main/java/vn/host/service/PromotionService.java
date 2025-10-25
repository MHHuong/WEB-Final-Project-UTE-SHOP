package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.Category;
import vn.host.entity.Promotion;

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

}
