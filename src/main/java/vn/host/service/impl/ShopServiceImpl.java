package vn.host.service.impl;

import org.springframework.web.multipart.MultipartFile;
import vn.host.service.ShopService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.util.sharedenum.UserRole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepo;
    private final UserRepository userRepo;

    @Override
    public void save(Shop shop) {
        shopRepo.save(shop);
    }

    @Override
    public void delete(long id) {
        shopRepo.deleteById(id);
    }

    @Override
    public List<Shop> findAll() {
        return shopRepo.findAll();
    }

    @Override
    public Shop findById(long id) {
        return shopRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Shop not found"));
    }

    @Override
    public Shop getMyShopOrNull(Long userId) {
        return shopRepo.findFirstByOwner_UserId(userId).orElse(null);
    }

    @Transactional
    public Shop registerOneShopForOwner(User owner, Shop incoming) {
        if (shopRepo.existsByOwner_UserId(owner.getUserId())) {
            throw new IllegalStateException("Owner already has a shop");
        }
        incoming.setOwner(owner);
        Shop saved = shopRepo.save(incoming);

        try {
            if (owner.getRole() == null || owner.getRole() != UserRole.SELLER) {
                owner.setRole(UserRole.SELLER);
                userRepo.save(owner);
            }
        } catch (Exception ignore) {
        }

        return saved;
    }

    @Override
    @Transactional
    public Shop updateMyShop(Long ownerUserId, java.util.function.Consumer<Shop> mutator) {
        Shop s = shopRepo.findForUpdateByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new NoSuchElementException("Shop not found"));
        mutator.accept(s);
        return shopRepo.save(s);
    }

    private Path uploadsRoot() {
        return Paths.get("uploads").toAbsolutePath().normalize();
    }

    private String extOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > -1 ? filename.substring(dot).toLowerCase() : "";
    }

    @Override
    @Transactional
    public String updateMyLogo(Long ownerUserId, MultipartFile file) throws IOException {
        Shop s = shopRepo.findForUpdateByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new NoSuchElementException("Shop not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        Path dir = uploadsRoot().resolve(Paths.get("shops", String.valueOf(ownerUserId)));
        Files.createDirectories(dir);

        String ext = extOf(file.getOriginalFilename() != null ? file.getOriginalFilename() : "png");
        if (ext.isBlank()) ext = ".png";
        String filename = "logo-" + Instant.now().toEpochMilli() + ext;

        if (s.getLogo() != null && s.getLogo().startsWith("/uploads/shops/" + ownerUserId + "/")) {
            try {
                Path old = uploadsRoot().resolve(s.getLogo().substring(1));
                Files.deleteIfExists(old);
            } catch (Exception ignore) {}
        }

        Path target = dir.resolve(filename).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/uploads/shops/" + ownerUserId + "/" + filename;
        s.setLogo(publicUrl);
        shopRepo.save(s);

        return publicUrl;
    }

    @Override
    @Transactional
    public void deleteMyLogo(Long ownerUserId) throws IOException {
        Shop s = shopRepo.findForUpdateByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new NoSuchElementException("Shop not found"));

        if (s.getLogo() != null && s.getLogo().startsWith("/uploads/")) {
            try {
                Path p = uploadsRoot().resolve(s.getLogo().substring(1));
                Files.deleteIfExists(p);
            } catch (Exception ignore) {}
        }
        s.setLogo(null);
        shopRepo.save(s);
    }
}
