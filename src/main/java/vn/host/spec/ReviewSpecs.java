package vn.host.spec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.host.entity.Product;
import vn.host.entity.Review;
import vn.host.entity.User;

public final class ReviewSpecs {
    private ReviewSpecs() {
    }

    // review thuộc các sản phẩm của shop (theo owner đăng nhập)
    public static Specification<Review> belongsToShop(Long shopId) {
        return (root, q, cb) -> cb.equal(root.get("product").get("shop").get("shopId"), shopId);
    }

    public static Specification<Review> qContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }

            // join để tìm theo tên product / user (LEFT để không loại bản ghi thiếu liên kết)
            Join<Review, Product> pJoin = root.join("product", JoinType.LEFT);
            Join<Review, User> uJoin = root.join("user", JoinType.LEFT);

            String like = "%" + q.toLowerCase() + "%";

            // null-safe -> coalesce(..., ""), rồi ép về VARCHAR bằng concat(..., "")
            Expression<String> commentExpr =
                    cb.lower(cb.concat(cb.coalesce(root.get("comment"), ""), ""));
            Expression<String> productNameExpr =
                    cb.lower(cb.concat(cb.coalesce(pJoin.get("name"), ""), ""));
            Expression<String> userNameExpr =
                    cb.lower(cb.concat(cb.coalesce(uJoin.get("fullName"), ""), ""));

            return cb.or(
                    cb.like(commentExpr, like),
                    cb.like(productNameExpr, like),
                    cb.like(userNameExpr, like)
            );
        };
    }

    // lọc đúng số sao
    public static Specification<Review> ratingEquals(Integer star) {
        if (star == null) return (root, q, cb) -> cb.conjunction();
        return (root, q, cb) -> cb.equal(root.get("rating"), star);
    }
}
