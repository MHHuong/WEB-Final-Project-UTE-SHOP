package vn.host.controller.shop;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.host.dto.product.ProductCreateReq;
import vn.host.entity.*;
import vn.host.repository.*;
import vn.host.service.ProductService;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/shop/products")
@RequiredArgsConstructor
public class ProductController {

    private final UserRepository users;
    private final ShopRepository shops;
    private final CategoryRepository categories;
    private final ProductMediaRepository mediaRepo;
    private final ProductService productService;

    private User authedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Unauthenticated");
        return users.findByEmail(auth.getName()).orElseThrow(() -> new SecurityException("User not found"));
    }

    private static Path uploadsRoot() {
        return Paths.get("uploads").toAbsolutePath().normalize();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createProduct(
            Authentication auth,
            @Valid @RequestPart("data") ProductCreateReq data,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) throws IOException {

        User u = authedUser(auth);
        Shop shop = shops.findFirstByOwner_UserId(u.getUserId())
                .orElseThrow(() -> new IllegalStateException("Bạn chưa đăng ký shop."));

        Category cat = categories.findById(data.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category không tồn tại"));

        Product p = new Product();
        p.setShop(shop);
        p.setCategory(cat);
        p.setName(data.getName());
        p.setDescription(data.getDescription());
        p.setPrice(data.getPrice());
        p.setStock(data.getStock() != null ? data.getStock() : 0);
        p.setStatus(data.getStatus() != null ? data.getStatus() : 0);

        if (!hasAtLeastOneImage(files)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "A product has to have at least one image file"
            );
        }

        productService.save(p);

        if (files != null && files.length > 0) {
            Path base = uploadsRoot().resolve("products").resolve(String.valueOf(p.getProductId())).normalize();
            Path imgDir = base.resolve("images");
            Path vidDir = base.resolve("videos");
            Files.createDirectories(imgDir);
            Files.createDirectories(vidDir);

            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) continue;

                boolean isImage = isImageFile(f);
                boolean isVideo = isVideoFile(f);
                if (!isImage && !isVideo) continue;

                String ext = guessExt(f, isVideo ? ".mp4" : ".png");
                String filename = (isVideo ? "vid-" : "img-")
                        + java.time.Instant.now().toEpochMilli() + "-" + java.util.UUID.randomUUID() + ext;

                java.nio.file.Path target = (isVideo ? vidDir : imgDir).resolve(filename);
                java.nio.file.Files.copy(f.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String publicUrl = "/uploads/products/" + p.getProductId()
                        + (isVideo ? "/videos/" : "/images/") + filename;

                var m = new ProductMedia();
                m.setProduct(p);
                m.setUrl(publicUrl);
                m.setType(isVideo ? vn.host.util.sharedenum.MediaType.video
                        : vn.host.util.sharedenum.MediaType.image);
                mediaRepo.save(m);
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("productId", p.getProductId());
        body.put("name", p.getName());
        body.put("categoryId", p.getCategory().getCategoryId());
        body.put("price", p.getPrice());
        body.put("stock", p.getStock());
        body.put("status", p.getStatus());
        body.put("description", p.getDescription());
        body.put("createdAt", p.getCreatedAt());
        body.put("media", mediaRepo.findByProduct_ProductId(p.getProductId())
                .stream().map(m -> Map.of("id", m.getMediaId(), "url", m.getUrl(), "type", m.getType().name()))
                .toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    private boolean hasAtLeastOneImage(MultipartFile[] files) {
        if (files == null || files.length == 0) return false;
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty() && isImageFile(f)) return true;
        }
        return false;
    }

    private boolean isImageFile(MultipartFile f) {
        String ct = java.util.Optional.ofNullable(f.getContentType()).orElse("");
        if (ct.startsWith("image/")) return true;
        String ext = org.springframework.util.StringUtils.getFilenameExtension(f.getOriginalFilename());
        return ext != null && java.util.Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "avif")
                .contains(ext.toLowerCase());
    }

    private boolean isVideoFile(MultipartFile f) {
        String ct = java.util.Optional.ofNullable(f.getContentType()).orElse("");
        if (ct.startsWith("video/")) return true;
        String ext = org.springframework.util.StringUtils.getFilenameExtension(f.getOriginalFilename());
        return ext != null && java.util.Set.of("mp4", "webm", "mov", "mkv", "avi")
                .contains(ext.toLowerCase());
    }

    private String guessExt(MultipartFile f, String fallback) {
        String original = f.getOriginalFilename();
        if (original != null && original.lastIndexOf('.') >= 0) {
            return original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        return fallback;
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(Authentication auth, @PathVariable long productId) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Unauthenticated");
        productService.softDeleteOwnerProduct(auth.getName(), productId);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/{productId}/restore")
    public ResponseEntity<Void> restoreProduct(
            Authentication auth,
            @PathVariable long productId,
            @RequestParam(defaultValue = "0") int toStatus
    ) {
        if (auth == null || auth.getName() == null) throw new SecurityException("Unauthenticated");
        productService.restoreOwnerProduct(auth.getName(), productId, toStatus);
        return ResponseEntity.noContent().build();
    }
}