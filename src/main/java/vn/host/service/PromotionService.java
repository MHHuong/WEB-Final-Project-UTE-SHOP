package vn.host.service;

import vn.host.entity.Promotion;

import java.util.List;

public interface PromotionService {
    List<Promotion> findGlobalPromotions();
    Promotion save(Promotion promotion);
    Promotion update(Long id, Promotion promotion);
    void delete(Long id);
}
