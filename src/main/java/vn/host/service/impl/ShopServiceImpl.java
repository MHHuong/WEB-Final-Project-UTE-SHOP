package vn.host.service.impl;

import vn.host.service.ShopService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.host.entity.Shop;
import vn.host.entity.User;
import vn.host.repository.ShopRepository;
import vn.host.repository.UserRepository;
import vn.host.util.sharedenum.UserRole;

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

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

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

    @Transactional
    public Shop updateMyShop(Long ownerUserId, java.util.function.Consumer<Shop> mutator) {
        Shop s = shopRepo.findForUpdateByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new NoSuchElementException("Shop not found"));
        mutator.accept(s);
        return shopRepo.save(s);
    }
}
