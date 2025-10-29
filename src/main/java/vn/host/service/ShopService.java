package vn.host.service;

import org.springframework.web.multipart.MultipartFile;
import vn.host.entity.Shop;
import vn.host.entity.User;

import java.io.IOException;
import java.util.List;

public interface ShopService {
    void save(Shop shop);

    void delete(long id);

    List<Shop> findAll();

    Shop findById(long id);

    Shop getMyShopOrNull(Long userId);

    Shop registerOneShopForOwner(User owner, Shop incoming);

    Shop updateMyShop(Long ownerUserId, java.util.function.Consumer<Shop> mutator);

    String updateMyLogo(Long ownerUserId, MultipartFile file) throws IOException;

    void deleteMyLogo(Long ownerUserId) throws IOException;

    Shop findFirstByOwner_UserId(Long userId);

    Shop findShopByOwner_UserId(Long userId);
}
