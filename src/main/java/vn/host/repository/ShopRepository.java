package vn.host.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.host.entity.Shop;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop,Long>, JpaSpecificationExecutor<Shop> {
    Optional<Shop> findFirstByOwner_UserId(Long userId);

    boolean existsByOwner_UserId(Long userId);

    List<Shop> findAllByOwner_UserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shop s where s.owner.userId = :userId")
    Optional<Shop> findForUpdateByOwnerUserId(@Param("userId") Long userId);
}
