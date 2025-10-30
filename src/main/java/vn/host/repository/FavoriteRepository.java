package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.host.entity.Favorite;
import vn.host.entity.FavoriteId;
import vn.host.entity.Product;
import vn.host.entity.User;
import vn.host.model.response.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId>, JpaSpecificationExecutor<Favorite> {
    boolean existsById_UserIdAndId_ProductId(Long userId, Long productId);

    @Transactional
    @Modifying
    void deleteByUserAndProduct(User user, Product product);

    @Transactional
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.id.userId = :userId AND f.id.productId = :productId")
    void deleteByUserIdAndProductId(Long userId, Long productId);

    List<Favorite> findByUser_UserId(Long userId);
}
