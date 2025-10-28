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
import vn.host.entity.Category;
import vn.host.entity.Promotion;
import vn.host.repository.CategoryRepository;
import vn.host.repository.PromotionRepository;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.service.PromotionService;
import vn.host.spec.PromotionSpecs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    // ===================== LẤY DANH SÁCH =====================
    @Override
    public Page<Promotion> getAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return promotionRepository.findAll(pageable);
        }
        return promotionRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    // ===================== TÌM THEO ID =====================
    @Override
    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình khuyến mãi"));
    }

    // ===================== THÊM MỚI =====================
    @Override
    public Promotion save(Map<String, Object> body) {
        Promotion promotion = new Promotion();

        promotion.setTitle((String) body.get("title"));
        promotion.setDescription((String) body.get("description"));
        promotion.setDiscountPercent(new BigDecimal(body.get("discountPercent").toString()));
        promotion.setStartDate(LocalDate.parse((String) body.get("startDate")));
        promotion.setEndDate(LocalDate.parse((String) body.get("endDate")));

        LocalDate today = LocalDate.now();

        // ====== VALIDATION ======
        if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        // Nếu ngày bắt đầu trong quá khứ → cấm
        if (promotion.getStartDate().isBefore(today)) {
            throw new RuntimeException("Không thể tạo chương trình với ngày bắt đầu trong quá khứ");
        }

        // Nếu ngày bắt đầu là hôm nay → cho phép nhưng cảnh báo
        if (promotion.getStartDate().isEqual(today)) {
            System.out.println("⚠️ Cảnh báo: Khuyến mãi bắt đầu ngay hôm nay. Hãy đảm bảo chương trình đã được kiểm tra kỹ!");
        }

        // ====== GÁN DANH MỤC ======
        if (body.get("applyCategoryId") != null) {
            Long categoryId = Long.parseLong(body.get("applyCategoryId").toString());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID = " + categoryId));
            promotion.setApplyCategory(category);
        }

        promotion.setShop(null);
        return promotionRepository.save(promotion);
    }

    // ===================== CẬP NHẬT =====================
    @Override
    public Promotion update(Long id, Map<String, Object> body) {
        Promotion existing = findById(id);
        LocalDate today = LocalDate.now();

        // ========== Nếu chương trình đã hoặc đang diễn ra ==========
        if (!existing.getStartDate().isAfter(today)) {
            boolean changingSensitive = false;

            // kiểm tra có thực sự thay đổi ngày bắt đầu
            if (body.get("startDate") != null) {
                LocalDate newStart = LocalDate.parse((String) body.get("startDate"));
                if (!newStart.equals(existing.getStartDate())) {
                    changingSensitive = true;
                }
            }

            // kiểm tra có thay đổi ngày kết thúc
            if (body.get("endDate") != null) {
                LocalDate newEnd = LocalDate.parse((String) body.get("endDate"));
                if (!newEnd.equals(existing.getEndDate())) {
                    changingSensitive = true;
                }
            }

            // kiểm tra có thay đổi mức giảm
            if (body.get("discountPercent") != null) {
                BigDecimal newDiscount = new BigDecimal(body.get("discountPercent").toString());
                if (existing.getDiscountPercent() == null || existing.getDiscountPercent().compareTo(newDiscount) != 0) {
                    changingSensitive = true;
                }
            }

            // kiểm tra có thay đổi danh mục
            if (body.get("applyCategoryId") != null) {
                Long newCatId = Long.parseLong(body.get("applyCategoryId").toString());
                Long oldCatId = existing.getApplyCategory() != null ? existing.getApplyCategory().getCategoryId() : null;
                if (!newCatId.equals(oldCatId)) {
                    changingSensitive = true;
                }
            }

            // Nếu có thay đổi phần nhạy cảm thì chặn
            if (changingSensitive) {
                throw new RuntimeException("Không thể chỉnh sửa ngày, mức giảm hoặc danh mục khi khuyến mãi đang diễn ra hoặc đã hết hạn!");
            }
        }

        // ========== Cho phép sửa tiêu đề và mô tả ==========
        if (body.get("title") != null)
            existing.setTitle((String) body.get("title"));
        if (body.get("description") != null)
            existing.setDescription((String) body.get("description"));

        // ========== Nếu chương trình chưa bắt đầu thì cho phép chỉnh sửa đầy đủ ==========
        if (existing.getStartDate().isAfter(today)) {
            if (body.get("discountPercent") != null)
                existing.setDiscountPercent(new BigDecimal(body.get("discountPercent").toString()));
            if (body.get("startDate") != null)
                existing.setStartDate(LocalDate.parse((String) body.get("startDate")));
            if (body.get("endDate") != null)
                existing.setEndDate(LocalDate.parse((String) body.get("endDate")));

            if (existing.getStartDate().isAfter(existing.getEndDate())) {
                throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc");
            }

            if (body.get("applyCategoryId") != null) {
                Long categoryId = Long.parseLong(body.get("applyCategoryId").toString());
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID = " + categoryId));
                existing.setApplyCategory(category);
            } else {
                existing.setApplyCategory(null);
            }
        }

        existing.setShop(null);
        return promotionRepository.save(existing);
    }

    // ===================== XÓA =====================
    @Override
    public void delete(Long id) {
        Promotion p = findById(id);
        LocalDate today = LocalDate.now();

        // Nếu hôm nay nằm trong khoảng diễn ra → không cho xóa
        if ((today.isEqual(p.getStartDate()) || today.isAfter(p.getStartDate()))
                && (today.isBefore(p.getEndDate()) || today.isEqual(p.getEndDate()))) {
            throw new RuntimeException("Không thể xóa chương trình đang diễn ra");
        }

        // Cho phép xóa nếu đã hết hạn hoặc chưa bắt đầu
        promotionRepository.delete(p);
    }

    // ===================== KHÁC =====================
    @Override
    public List<Promotion> getActivePromotions(Category category) {
        return promotionRepository.findActivePromotions(category, LocalDate.now());
    }

    @Override
    public String getStatus(Promotion promotion) {
        LocalDate now = LocalDate.now();
        if (now.isBefore(promotion.getStartDate())) return "Sắp diễn ra";
        if (now.isAfter(promotion.getEndDate())) return "Hết hạn";
        return "Đang diễn ra";
    }

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
