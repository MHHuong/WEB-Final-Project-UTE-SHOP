package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.host.entity.Favorite;
import vn.host.entity.FavoriteId;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId>, JpaSpecificationExecutor<Favorite> {
    boolean existsById_UserIdAndId_ProductId(Long userId, Long productId);
    void deleteById_UserIdAndId_ProductId(Long userId, Long productId);
    Page<Favorite> findById_UserId(Long userId, Pageable pageable);
}
