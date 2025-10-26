package vn.host.controller.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.review.ReviewItemRes;
import vn.host.dto.review.ReviewMediaRes;
import vn.host.entity.*;
import vn.host.repository.ReviewRepository;
import vn.host.service.ShopService;
import vn.host.service.UserService;

import jakarta.persistence.criteria.Join;

import java.util.List;

@RestController
@RequestMapping("/api/shop/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final UserService userService;
    private final ShopService shopService;
    private final ReviewRepository reviewRepo;

    // Lấy user từ Authentication (giống ShopController của bạn)
    private User authedUser(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        String email = auth.getName();
        return userService.getUserByEmail(email);
    }

    @GetMapping
    public ResponseEntity<PageResult<ReviewItemRes>> list(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,       // lọc theo sao
            @RequestParam(required = false) Long productId,       // lọc theo product
            @RequestParam(required = false) String q,             // tìm trong comment / user name
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort
    ) {
        User u = authedUser(auth);
        Shop myShop = shopService.getMyShopOrNull(u.getUserId());
        if (myShop == null) {
            return ResponseEntity.ok(PageResult.<ReviewItemRes>builder()
                    .content(List.of())
                    .page(page).size(size)
                    .totalElements(0).totalPages(0)
                    .build());
        }

        Sort s = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), s);

        // Build Specification: review -> product -> shop == myShop
        Specification<Review> spec = (root, query, cb) -> {
            Join<Review, Product> p = root.join("product");
            Join<Product, Shop> shp = p.join("shop");

            var predicates = new java.util.ArrayList<>(List.of(
                    cb.equal(shp.get("shopId"), myShop.getShopId())
            ));

            if (productId != null) {
                predicates.add(cb.equal(p.get("productId"), productId));
            }
            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }
            if (StringUtils.hasText(q)) {
                // tìm trong comment hoặc tên người review (user.fullName)
                // review có field user? (entity Review của bạn có quan hệ user -> ở file bị rút gọn
                // nên ta join nếu tồn tại)
                try {
                    Join<Review, User> ru = root.join("user");
                    var like = "%" + q.trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("comment")), like),
                            cb.like(cb.lower(ru.get("fullName")), like)
                    ));
                } catch (Exception ignored) {
                    // fallback: chỉ tìm trong comment
                    var like = "%" + q.trim().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(root.get("comment")), like));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Review> pageData = reviewRepo.findAll(spec, pageable);

        List<ReviewItemRes> items = pageData.getContent().stream().map(r -> {
            Product p = r.getProduct();
            User reviewer = null;
            try {
                reviewer = (User) Review.class.getMethod("getUser").invoke(r);
            } catch (Exception ignored) {
            }
            
            final User reviewerRef = reviewer;

            return ReviewItemRes.builder()
                    .reviewId(getLongSafely(() -> (Long) Review.class.getMethod("getReviewId").invoke(r)))
                    .productId(p != null ? getLongSafely(() -> (Long) Product.class.getMethod("getProductId").invoke(p)) : null)
                    .productName(p != null ? getStringSafely(() -> (String) Product.class.getMethod("getName").invoke(p)) : null)
                    .userId(reviewerRef != null ? getLongSafely(() ->
                            (Long) User.class.getMethod("getUserId").invoke(reviewerRef)
                    ) : null)
                    .userName(reviewerRef != null ? getStringSafely(() ->
                            (String) User.class.getMethod("getFullName").invoke(reviewerRef)
                    ) : null)
                    .rating(r.getRating())
                    .comment(r.getComment())
                    .createdAt(r.getCreatedAt())
                    .media(r.getMedia() == null ? List.of() :
                            r.getMedia().stream().map(m ->
                                    ReviewMediaRes.builder()
                                            .id(getLongSafely(() -> (Long) m.getClass().getMethod("getReviewMediaId").invoke(m)))
                                            .url(getStringSafely(() -> (String) m.getClass().getMethod("getUrl").invoke(m)))
                                            .type(getStringSafely(() -> String.valueOf(m.getClass().getMethod("getType").invoke(m))))
                                            .build()
                            ).toList()
                    )
                    .build();
        }).toList();

        return ResponseEntity.ok(PageResult.<ReviewItemRes>builder()
                .content(items)
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build());
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return switch (field) {
            case "rating" -> Sort.by(dir, "rating");
            case "product" -> Sort.by(dir, "product.name");
            case "user" -> Sort.by(dir, "user.fullName");
            default -> Sort.by(dir, "createdAt");
        };
    }

    // vài helper nho nhỏ để tránh NPE do source bị rút gọn (…)
    private Long getLongSafely(java.util.concurrent.Callable<Long> c) {
        try {
            return c.call();
        } catch (Exception e) {
            return null;
        }
    }

    private String getStringSafely(java.util.concurrent.Callable<String> c) {
        try {
            return c.call();
        } catch (Exception e) {
            return null;
        }
    }
}