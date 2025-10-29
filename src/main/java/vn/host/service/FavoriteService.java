package vn.host.service;

import vn.host.dto.ProductDTO;
import vn.host.entity.Favorite;
import vn.host.entity.FavoriteId;
import vn.host.model.response.ProductResponse;

import java.util.List;

public interface FavoriteService {
    List<ProductDTO> findByUserId(long userId);

    Favorite addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);
}
