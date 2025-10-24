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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final PasswordEncoder pe;
    private final AuthenticationManager am;
    private final JwtService jwt;
    private final OtpService otp;
    private final EmailService mail;

    @Override
    public void register(RegisterReq req) {
        users.findByEmail(req.email()).ifPresent(u -> { throw new RuntimeException("Email đã tồn tại"); });
        User u = new User();
        u.setEmail(req.email());
        u.setPasswordHash(pe.encode(req.password()));
        u.setFullName(req.fullName());
        u.setRole(UserRole.USER);
        users.save(u);
    }

    @Override
    public AuthRes login(LoginReq req) {
        Authentication a = am.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User u = users.findByEmail(req.email()).orElseThrow();
        Map<String, Object> claims = Map.of(
                "role", u.getRole().name(),
                "name", u.getFullName(),
                "userId", u.getUserId() // <-- Thêm userId vào claims
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
    public boolean verifyOtp(VerifyOtpReq req) { return otp.verify(req.email(), req.otp()); }

    @Override
    public void resetPassword(ResetPasswordReq req) {
        if (!otp.verify(req.email(), req.otp())) throw new RuntimeException("OTP không hợp lệ/đã hết hạn");
        User u = users.findByEmail(req.email()).orElseThrow();
        u.setPasswordHash(pe.encode(req.newPassword()));
        users.save(u);
    }
}