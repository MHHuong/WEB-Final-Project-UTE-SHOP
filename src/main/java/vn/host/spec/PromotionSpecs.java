package vn.host.spec;

import org.springframework.data.jpa.domain.Specification;
import vn.host.entity.Promotion;

import java.time.LocalDate;

public final class PromotionSpecs {
    private PromotionSpecs() {
    }

    public static Specification<Promotion> belongsToShop(Long shopId) {
        return (root, q, cb) -> cb.equal(root.get("shop").get("shopId"), shopId);
    }

    public static Specification<Promotion> titleContains(String qStr) {
        if (qStr == null) return (root, q, cb) -> cb.conjunction();
        String like = "%" + qStr.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like)
        );
    }

    public static Specification<Promotion> statusActive(LocalDate today) {
        return (root, q, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("startDate"), today),
                cb.greaterThanOrEqualTo(root.get("endDate"), today)
        );
    }

    public static Specification<Promotion> statusUpcoming(LocalDate today) {
        return (root, q, cb) -> cb.greaterThan(root.get("startDate"), today);
    }

    public static Specification<Promotion> statusExpired(LocalDate today) {
        return (root, q, cb) -> cb.lessThan(root.get("endDate"), today);
    }
}