package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.host.dto.auth.*;
import vn.host.entity.User;
import vn.host.repository.UserRepository;
import vn.host.security.JwtService;
import vn.host.service.AuthService;
import vn.host.service.EmailService;
import vn.host.service.OtpService;
import vn.host.util.sharedenum.UserRole;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final PasswordEncoder pe;
    private final ObjectProvider<AuthenticationManager> amProvider;
    private final JwtService jwt;
    private final OtpService otp;
    private final EmailService mail;

    @Override
    public void register(RegisterReq req) {
        if (!otp.verify(req.getEmail(), req.getOtp())) {
            throw new RuntimeException("OTP không hợp lệ hoặc đã hết hạn");
        }
        users.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email đã tồn tại");
        });
        if (req.getPassword() == null || !req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu không khớp");
        }
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPasswordHash(pe.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setPhone(req.getPhone());
        u.setRole(UserRole.USER);
        users.save(u);
    }

    @Override
    public AuthRes login(LoginReq req) {
        AuthenticationManager am = amProvider.getObject();
        Authentication a = am.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User u = users.findByEmail(req.email()).orElseThrow();

        if (u.getStatus() != null && u.getStatus() == 0) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên!");
        }

        Map<String, Object> claims = Map.of(
                "role", u.getRole().name(),
                "name", u.getFullName(),
                "userId", u.getUserId()
        );
        String token = jwt.generate(u.getEmail(), claims);
        return new AuthRes(token, u.getEmail(), u.getFullName(), u.getRole().name(), u.getUserId());
    }

    @Override
    public void requestOtp(EmailOnlyReq req) {
        users.findByEmail(req.email()).orElseThrow(() -> new RuntimeException("Email chưa đăng ký"));
        String code = otp.issue(req.email());
        mail.sendOtp(req.email(), code);
    }

    @Override
    public boolean verifyOtp(VerifyOtpReq req) {
        return otp.verify(req.email(), req.otp());
    }

    @Override
    public void resetPassword(ResetPasswordReq req) {
        if (!otp.verify(req.email(), req.otp())) throw new RuntimeException("OTP không hợp lệ/đã hết hạn");
        User u = users.findByEmail(req.email()).orElseThrow();
        u.setPasswordHash(pe.encode(req.newPassword()));
        users.save(u);
    }

    @Override
    public void requestRegistrationOtp(EmailOnlyReq req) {
        users.findByEmail(req.email()).ifPresent(u -> {
            throw new RuntimeException("Email này đã được đăng ký");
        });
        String code = otp.issue(req.email());
        mail.sendOtp(req.email(), code);
    }

    @Override
    public AuthRes processOAuth2User(String email, String name) {
        User user = users.findByEmail(email)
                .orElseGet(() -> createOAuth2User(email, name));
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "name", user.getFullName(),
                "userId", user.getUserId()
        );
        String token = jwt.generate(user.getEmail(), claims);
        return new AuthRes(token, user.getEmail(), user.getFullName(), user.getRole().name(), user.getUserId());
    }

    private User createOAuth2User(String email, String name) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setRole(UserRole.USER);
        newUser.setPasswordHash(null);
        newUser.setPhone(null);
        return users.save(newUser);
    }
}