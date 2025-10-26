package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.Promotion;
import vn.host.repository.PromotionRepository;
import vn.host.service.PromotionService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;

    @Override
    public void save(Promotion promotion) {
        promotionRepository.save(promotion);
    }

    @Override
    public Promotion findById(long id) {
        return promotionRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Promotion> findByShop_ShopId(long shopId, Pageable pageable) {
        return promotionRepository.findByShop_ShopId(shopId, pageable);
    }

    @Override
    public boolean existsOverlappingGlobal(Long shopId, LocalDate startDate, LocalDate endDate, Long ignoreId) {
        return promotionRepository.existsOverlappingGlobal(shopId, startDate, endDate, ignoreId);
    }

    @Override
    public boolean existsOverlappingForCategory(Long shopId, Long categoryId, LocalDate startDate, LocalDate endDate, Long ignoreId) {
        return promotionRepository.existsOverlappingForCategory(shopId, categoryId, startDate, endDate, ignoreId);
    }
}
