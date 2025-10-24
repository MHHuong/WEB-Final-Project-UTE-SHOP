package vn.host.service;

import vn.host.dto.auth.*;

public interface AuthService {
    void register(RegisterReq req);
    AuthRes login(LoginReq req);
    void requestOtp(EmailOnlyReq req);
    boolean verifyOtp(VerifyOtpReq req);
    void resetPassword(ResetPasswordReq req);
}