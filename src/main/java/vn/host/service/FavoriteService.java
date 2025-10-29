package vn.host.service;

import vn.host.entity.Favorite;
import vn.host.entity.FavoriteId;

import java.util.List;

public interface FavoriteService {
    void save(Favorite favorite);
    void delete(FavoriteId id);
    List<Favorite> findAll();
    List<Favorite> findByUserId(long userId);
}
