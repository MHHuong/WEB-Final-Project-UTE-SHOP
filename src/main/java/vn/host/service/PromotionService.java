package vn.host.service;

import vn.host.entity.Promotion;

import java.util.List;

public interface PromotionService {
    void save(Promotion promotion);
    void delete(long id);
    List<Promotion> findAll();
    Promotion findById(long id);
}
