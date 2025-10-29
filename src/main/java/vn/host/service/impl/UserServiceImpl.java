package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.host.entity.User;
import vn.host.model.request.PasswordRequest;
import vn.host.model.request.UserRequest;
import vn.host.repository.UserRepository;
import vn.host.service.UserService;

import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder pe;

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    @Override
    public void save(User user) {
        userRepo.save(user);
    }

    @Override
    public User saveInfoUser(UserRequest userRequest, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        User existingUser = userRepo.findByEmail(userRequest.getEmail()).orElse(null);
        if (existingUser != null && !existingUser.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Email is already in use by another user");
        }
        user.setFullName(userRequest.getFullName());
        user.setPhone(userRequest.getPhone());
        user.setEmail(userRequest.getEmail());
        return userRepo.save(user);
    }

    @Override
    public void updatePassword(Long UserId, PasswordRequest newPassword) {
        // Encode password trước khi lưu
        User user = userRepo.findById(UserId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        System.out.println(newPassword);
        if (newPassword == null) {
            throw new IllegalArgumentException("New password is required");
        }
        if (newPassword.getCurrentPassword() == null) {
            throw new IllegalArgumentException("Current password is required");
        }
        String currentPass = newPassword.getCurrentPassword();
        if (!pe.matches(currentPass, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (newPassword.getNewPassword() == null) {
            throw new IllegalArgumentException("New password is required");
        }
        String newPasswordValue = newPassword.getNewPassword();
        if (pe.matches(newPasswordValue, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the old password");
        }
        user.setPasswordHash(pe.encode(newPasswordValue));
        userRepo.save(user);
    }
}
