package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.Promotion;
import vn.host.repository.PromotionRepository;
import vn.host.service.PromotionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public List<Promotion> findGlobalPromotions() {
        return promotionRepository.findByShopIsNull();
    }

    @Override
    public Promotion save(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion update(Long id, Promotion promotion) {
        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        existing.setTitle(promotion.getTitle());
        existing.setDescription(promotion.getDescription());
        existing.setDiscountPercent(promotion.getDiscountPercent());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());
        existing.setApplyCategory(promotion.getApplyCategory());
        return promotionRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }
}
