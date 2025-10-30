package vn.host.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.dto.auth.*;
import vn.host.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService svc;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterReq req) {
        svc.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@RequestBody @Valid LoginReq req) {
        AuthRes res = svc.login(req);
        ResponseCookie cookie = ResponseCookie.from("AUTH_TOKEN", res.token())
                .path("/")
                .httpOnly(true)
                .sameSite("Lax") // nếu frontend khác origin, dùng "None" và set Secure=true
                .maxAge(7 * 24 * 60 * 60) // tuỳ ý
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }

    @PostMapping("/otp/request")
    public ResponseEntity<?> requestOtp(@RequestBody @Valid EmailOnlyReq req) {
        svc.requestOtp(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Boolean> verify(@RequestBody @Valid VerifyOtpReq req) {
        return ResponseEntity.ok(svc.verifyOtp(req));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(@RequestBody @Valid ResetPasswordReq req) {
        svc.resetPassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/register")
    public ResponseEntity<?> requestRegistrationOtp(@RequestBody @Valid EmailOnlyReq req) {
        svc.requestRegistrationOtp(req);
        return ResponseEntity.ok().build();
    }
}