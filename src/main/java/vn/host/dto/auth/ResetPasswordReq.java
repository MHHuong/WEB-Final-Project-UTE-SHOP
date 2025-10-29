package vn.host.dto.auth;

import jakarta.validation.constraints.*;
public record ResetPasswordReq(@Email String email, @NotBlank String newPassword, @NotBlank String otp) {}