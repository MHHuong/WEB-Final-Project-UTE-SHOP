package vn.host.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.host.entity.User;
import vn.host.util.sharedenum.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByEmailContainingIgnoreCase(String email);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.status = 1")
    List<User> findAllActiveUsers();

    Page<User> findAll(Pageable pageable);

    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    User findUserByFullName(String fullName);
}
