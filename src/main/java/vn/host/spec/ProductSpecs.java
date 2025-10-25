package vn.host.spec;

import org.springframework.data.jpa.domain.Specification;
import vn.host.entity.Product;

import java.math.BigDecimal;

public final class ProductSpecs {
    public static Specification<Product> belongsToShop(Long shopId) {
        return (root, q, cb) -> cb.equal(root.get("shop").get("shopId"), shopId);
    }

    public static Specification<Product> nameContains(String qStr) {
        return (root, q, cb) -> cb.like(cb.lower(root.get("name")), "%" + qStr.toLowerCase() + "%");
    }

    public static Specification<Product> categoryIs(Long categoryId) {
        return (root, q, cb) -> cb.equal(root.get("category").get("categoryId"), categoryId);
    }

    public static Specification<Product> statusIs(Integer status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Product> priceGte(BigDecimal min) {
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceLte(BigDecimal max) {
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }
}