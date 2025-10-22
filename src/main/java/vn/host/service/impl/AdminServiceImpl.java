package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.host.entity.User;
import vn.host.repository.UserRepository;
import vn.host.service.AdminService;
import vn.host.util.sharedenum.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> searchUsersByEmail(String keyword) {
        return userRepository.findByEmailContainingIgnoreCase(keyword);
    }

    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nếu là ADMIN thì chặn
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Không thể khóa tài khoản ADMIN!");
        }

        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public void updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Không thể thay đổi vai trò sang ADMIN!");
        }
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (target.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Không thể xóa tài khoản ADMIN khác!");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User saveUser(User user) {
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Không thể tạo tài khoản ADMIN khác!");
        }
        // Mã hóa mật khẩu nếu là raw text
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }

    @Override
    public User updateUserInfo(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Không thể chỉnh sửa tài khoản ADMIN khác!");
        }
        user.setFullName(updatedUser.getFullName());
        user.setPhone(updatedUser.getPhone());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());
        user.setStatus(updatedUser.getStatus());

        // nếu có password mới => mã hóa lại
        if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
        }

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsersByEmail(String keyword, Pageable pageable) {
        return userRepository.findByEmailContainingIgnoreCase(keyword, pageable);
    }
}
