package vn.host.controller.shop;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.host.dto.common.PageResult;
import vn.host.dto.product.ProductMediaVM;
import vn.host.dto.review.ReviewDetailVM;
import vn.host.dto.review.ReviewItemRes;
import vn.host.dto.review.ReviewMediaRes;
import vn.host.entity.*;
import vn.host.repository.*;
import vn.host.service.*;

import vn.host.spec.ReviewSpecs;
import vn.host.util.sharedenum.OrderStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/shop/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class ReviewController {

    private final UserService userService;
    private final ShopService shopService;
    private final ReviewService reviewService;
    private final ReviewMediaService reviewMediaService;
    private final ProductMediaService productMediaService;
    private final OrderService orderService;
    private final ProductService productService;

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

    @GetMapping(params = "productId")
    public ResponseEntity<PageResult<ReviewItemRes>> listByProduct(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        // parse sort
        Sort sortObj;
        try {
            String[] parts = sort.split(",");
            sortObj = (parts.length == 2)
                    ? Sort.by(Sort.Direction.fromString(parts[1].trim()), parts[0].trim())
                    : Sort.by(Sort.Direction.DESC, "createdAt");
        } catch (Exception e) {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // chỉ lọc theo productId, KHÔNG gọi authedUser, KHÔNG lọc theo shop
        Page<Review> pg = reviewService.findByProduct_ProductId(productId, pageable);

        // map về VM như bạn đang dùng
        List<ReviewItemRes> content = pg.getContent().stream().map(r -> ReviewItemRes.builder()
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

        PageResult<ReviewItemRes> result = PageResult.<ReviewItemRes>builder()
                .content(content)
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .build();

        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReview(
            Authentication auth,
            @RequestParam Long productId,
            @RequestParam Long orderId,
            @RequestParam @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment,
            @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthenticated"));
        }

        // 1) Xác thực user
        String email = auth.getName();
        User user = userService.findByEmail(email);

        // 2) Kiểm tra order thuộc user & đã RECEIVED
        Order order = orderService.findById(orderId);
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Order not belong to user"));
        }
        if (order.getStatus() != OrderStatus.RECEIVED) {
            return ResponseEntity.badRequest().body(Map.of("message", "Order not RECEIVED"));
        }

        // 3) Kiểm tra product có trong order
        boolean inOrder = order.getItems().stream().anyMatch(oi -> oi.getProduct().getProductId().equals(productId));
        if (!inOrder) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product not in this order"));
        }

        Product product = productService.findById(productId);

        Optional<Review> existed = reviewService.findFirstByUser_UserIdAndProduct_ProductIdOrderByCreatedAtDesc(
                user.getUserId(), productId);
        if (existed.isPresent()) {
            return updateReview(auth, existed.get().getReviewId(), rating, comment, files);
        }

        // 4) Tạo Review
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build();
        review = reviewService.save(review);

        // 5) Lưu file (nếu có)
        if (files != null && !files.isEmpty()) {
            Path root = uploadsRoot();
            Path dir = root.resolve(Path.of("reviews", String.valueOf(user.getUserId()), String.valueOf(review.getReviewId()))).normalize();
            Files.createDirectories(dir);

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // Phân loại media
                String contentType = Optional.ofNullable(file.getContentType()).orElse("");
                vn.host.util.sharedenum.MediaType mediaType = contentType.startsWith("video")
                        ? vn.host.util.sharedenum.MediaType.video
                        : vn.host.util.sharedenum.MediaType.image;

                String ext = "";
                String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
                int dot = original.lastIndexOf('.');
                if (dot >= 0) ext = original.substring(dot);

                String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + (ext.isBlank() ? "" : ext);
                Path target = dir.resolve(filename);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                String publicUrl = "/uploads/reviews/" + user.getUserId() + "/" + review.getReviewId() + "/" + filename;

                ReviewMedia rm = ReviewMedia.builder()
                        .review(review)
                        .url(publicUrl)
                        .type(mediaType)
                        .build();
                reviewMediaService.save(rm);
            }
        }

        // 6) Trả về item VM gọn nhẹ
        Review finalReview = review;
        List<ReviewMediaRes> mediaResList = reviewMediaService.findByReview_ReviewId(finalReview.getReviewId()).stream()
                .map(m -> ReviewMediaRes.builder()
                        .id(m.getReviewMediaId())
                        .url(m.getUrl())
                        .type(m.getType().name())
                        .build())
                .toList();

        ReviewItemRes res = ReviewItemRes.builder()
                .reviewId(finalReview.getReviewId())
                .productId(product.getProductId())
                .productName(product.getName())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .rating(finalReview.getRating())
                .comment(finalReview.getComment())
                .createdAt(finalReview.getCreatedAt())
                .media(mediaResList)
                .build();

        return ResponseEntity.ok(res);
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMyReview(Authentication auth,
                                         @RequestParam Long productId) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Unauthenticated"));
        }
        String email = auth.getName();
        User user = userService.findByEmail(email);

        Optional<Review> opt = reviewService.findFirstByUser_UserIdAndProduct_ProductIdOrderByCreatedAtDesc(
                user.getUserId(), productId);

        if (opt.isEmpty()) {
            return ResponseEntity.ok().body(null); // không có review
        }
        Review r = opt.get();
        List<ReviewMediaRes> media = reviewMediaService.findByReview_ReviewId(r.getReviewId())
                .stream().map(m -> ReviewMediaRes.builder()
                        .id(m.getReviewMediaId())
                        .url(m.getUrl())
                        .type(m.getType().name())
                        .build()).toList();

        ReviewItemRes res = ReviewItemRes.builder()
                .reviewId(r.getReviewId())
                .productId(r.getProduct().getProductId())
                .productName(r.getProduct().getName())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .media(media)
                .build();

        return ResponseEntity.ok(res);
    }

    @PutMapping(value = "/{reviewId}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> updateReview(Authentication auth,
                                          @PathVariable Long reviewId,
                                          @RequestParam @Min(1) @Max(5) Integer rating,
                                          @RequestParam(required = false) String comment,
                                          @RequestPart(name = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Unauthenticated"));
        }
        String email = auth.getName();
        User user = userService.findByEmail(email);

        Review r = reviewService.findById(reviewId);
        if (!r.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body(Collections.singletonMap("message", "Not owner"));
        }

        // cập nhật rating/comment
        r.setRating(rating);
        r.setComment(comment);
        Review saved = reviewService.save(r);

        // Nếu có files mới => xoá media cũ & thêm mới
        if (files != null && !files.isEmpty()) {
            reviewMediaService.deleteByReview_ReviewId(r.getReviewId());

            Path root = uploadsRoot();
            Path dir = root.resolve(Path.of("reviews", String.valueOf(user.getUserId()), String.valueOf(r.getReviewId()))).normalize();
            Files.createDirectories(dir);

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String contentType = Optional.ofNullable(file.getContentType()).orElse("");
                vn.host.util.sharedenum.MediaType mediaType = contentType.startsWith("video")
                        ? vn.host.util.sharedenum.MediaType.video
                        : vn.host.util.sharedenum.MediaType.image;

                String ext = "";
                String original = Optional.ofNullable(file.getOriginalFilename()).orElse("");
                int dot = original.lastIndexOf('.');
                if (dot >= 0) ext = original.substring(dot);

                String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + (ext.isBlank() ? "" : ext);
                Path target = dir.resolve(filename);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                String publicUrl = "/uploads/reviews/" + user.getUserId() + "/" + r.getReviewId() + "/" + filename;

                ReviewMedia rm = ReviewMedia.builder()
                        .review(r)
                        .url(publicUrl)
                        .type(mediaType)
                        .build();
                reviewMediaService.save(rm);
            }
        }

        // trả về lại item đã cập nhật
        List<ReviewMediaRes> media = reviewMediaService.findByReview_ReviewId(r.getReviewId()).stream()
                .map(m -> ReviewMediaRes.builder().id(m.getReviewMediaId()).url(m.getUrl()).type(m.getType().name()).build())
                .toList();

        ReviewItemRes res = ReviewItemRes.builder()
                .reviewId(r.getReviewId())
                .productId(r.getProduct().getProductId())
                .productName(r.getProduct().getName())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .media(media)
                .build();

        return ResponseEntity.ok(res);
    }

    // ==== private helpers / fields ====
    private Path uploadsRoot() {
        return Paths.get("uploads").toAbsolutePath().normalize();
    }

    @RequiredArgsConstructor
    private static class CreateReviewReq { // (không dùng nếu nhận multipart trực tiếp)
        public Long productId;
        public Long orderId;
        public Integer rating;
        public String comment;
    }

}