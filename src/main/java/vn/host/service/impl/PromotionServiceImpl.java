package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.host.entity.Category;
import vn.host.entity.Promotion;
import vn.host.repository.CategoryRepository;
import vn.host.repository.PromotionRepository;
import vn.host.service.PromotionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;

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
}
