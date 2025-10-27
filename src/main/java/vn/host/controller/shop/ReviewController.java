package vn.host.controller.shop;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductMediaVM;
import vn.host.dto.review.ReviewDetailVM;
import vn.host.dto.review.ReviewItemRes;
import vn.host.dto.review.ReviewMediaRes;
import vn.host.entity.*;
import vn.host.service.*;

import jakarta.persistence.criteria.Join;
import vn.host.spec.ReviewSpecs;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shop/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final UserService userService;
    private final ShopService shopService;
    private final ReviewService reviewService;
    private final ReviewMediaService reviewMediaService;
    private final ProductMediaService productMediaService;

    // Lấy user từ Authentication (giống ShopController của bạn)
    private User authedUser(Authentication auth) {
        if (auth == null) throw new SecurityException("Unauthenticated");
        String email = auth.getName();
        return userService.getUserByEmail(email);
    }

    @GetMapping
    public ResponseEntity<PageResult<ReviewItemRes>> search(
            Authentication auth,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer rating, // lọc theo sao
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        User user = authedUser(auth);
        var shop = shopService.findFirstByOwner_UserId(user.getUserId());

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        List<Specification<Review>> list = new ArrayList<>();
        list.add(ReviewSpecs.belongsToShop(shop.getShopId()));
        if (StringUtils.hasText(q)) {
            list.add(ReviewSpecs.qContains(q));
        }
        if (rating != null) {
            list.add(ReviewSpecs.ratingEquals(rating));
        }

        Specification<Review> spec = Specification.allOf(list);

        Page<Review> pg = reviewService.findAll(spec, pageable);

        var content = pg.getContent().stream().map(r -> ReviewItemRes.builder()
                .reviewId(r.getReviewId())
                .productId(r.getProduct() != null ? r.getProduct().getProductId() : null)
                .productName(r.getProduct() != null ? r.getProduct().getName() : null)
                .userId(r.getUser() != null ? r.getUser().getUserId() : null)
                .userName(r.getUser() != null ? r.getUser().getFullName() : null)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build()
        ).toList();

        var result = PageResult.<ReviewItemRes>builder()
                .content(content)
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();

        return ResponseEntity.ok(result);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    @GetMapping("/{reviewId}/detail")
    public ResponseEntity<ReviewDetailVM> getReviewDetail(
            Authentication auth, @PathVariable Long reviewId) {

        User u = authedUser(auth);
        Shop myShop = shopService.findFirstByOwner_UserId(u.getUserId());

        Review r = reviewService.findById(reviewId);

        // Bảo vệ: review phải thuộc product của shop mình
        if (r.getProduct() == null || r.getProduct().getShop() == null
                || !r.getProduct().getShop().getShopId().equals(myShop.getShopId())) {
            throw new SecurityException("Not your review");
        }

        // 1) Nếu có media của review -> dùng
        List<ProductMediaVM> gallery;

        List<ProductMediaVM> rmedias = reviewMediaService.findByReview_ReviewId(reviewId)
                .stream()
                .map(m -> new ProductMediaVM(
                        m.getReviewMediaId(),
                        m.getUrl(),
                        m.getType().name().toLowerCase() // "image" | "video" cho JS
                ))
                .toList();

        if (!rmedias.isEmpty()) {
            gallery = rmedias;
        } else {
            gallery = productMediaService.findByProduct_ProductId(r.getProduct().getProductId())
                    .stream()
                    .map(m -> new ProductMediaVM(
                            m.getMediaId(),
                            m.getUrl(),
                            m.getType().name().toLowerCase()
                    ))
                    .toList();
        }
        String categoryName = r.getProduct().getCategory() != null
                ? r.getProduct().getCategory().getName() : null;

        return ResponseEntity.ok(ReviewDetailVM.of(r, categoryName, gallery));
    }
}