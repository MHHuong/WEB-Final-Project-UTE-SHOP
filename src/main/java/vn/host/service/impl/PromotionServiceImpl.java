package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.host.dto.common.PageResult;
import vn.host.dto.promotion.PromotionVM;
import vn.host.entity.Promotion;
import vn.host.repository.PromotionRepository;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.service.PromotionService;
import vn.host.spec.PromotionSpecs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    public void save(Promotion promotion) {
        promotionRepository.save(promotion);
    }

    @Override
    public void delete(Long id) {
        promotionRepository.deleteById(id);
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

    @Override
    public PageResult<PromotionVM> searchOwnerPromotions(String userEmail, String q, String status, int page, int size, Sort sort) {
        var user = userRepository.findByEmail(userEmail).orElseThrow(() -> new SecurityException("User not found"));
        var shop = shopRepository.findFirstByOwner_UserId(user.getUserId()).orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));
        Long shopId = shop.getShopId();

        Pageable pageable = PageRequest.of(page, size, (sort == null) ? Sort.by(Sort.Direction.DESC, "startDate") : sort);

        List<Specification<Promotion>> list = new ArrayList<>();
        list.add(PromotionSpecs.belongsToShop(shopId));
        if (StringUtils.hasText(q)) list.add(PromotionSpecs.titleContains(q));

        LocalDate today = LocalDate.now();
        if (StringUtils.hasText(status)) {
            String s = status.trim().toLowerCase();
            if (s.equals("active")) list.add(PromotionSpecs.statusActive(today));
            else if (s.equals("upcoming")) list.add(PromotionSpecs.statusUpcoming(today));
            else if (s.equals("expired")) list.add(PromotionSpecs.statusExpired(today));
        }

        Specification<vn.host.entity.Promotion> spec = Specification.allOf(list);
        Page<vn.host.entity.Promotion> pg = promotionRepository.findAll(spec, pageable);

        var content = pg.getContent().stream().map(PromotionVM::of).toList();
        return PageResult.<PromotionVM>builder()
                .content(content)
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();
    }
}
