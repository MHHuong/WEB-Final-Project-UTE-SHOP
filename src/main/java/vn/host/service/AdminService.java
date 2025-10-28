package vn.host.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.host.entity.User;
import vn.host.util.sharedenum.UserRole;

import java.util.List;

public interface AdminService {
    // USER MANAGEMENT
    List<User> getAllUsers();
    List<User> searchUsersByEmail(String keyword);
    List<User> getUsersByRole(UserRole role);
    void updateUserStatus(Long userId, Integer status);
    void updateUserRole(Long userId, UserRole newRole);
    void deleteUser(Long userId);
    User saveUser(User user);
    User updateUserInfo(Long id, User updatedUser);
    User getUserById(Long id);
    Page<User> searchUsersByEmail(String keyword, Pageable pageable);
    Page<User> getAllUsers(Pageable pageable);
}
