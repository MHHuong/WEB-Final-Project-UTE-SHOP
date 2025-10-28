package vn.host.dto.auth;

import jakarta.validation.constraints.*;

public record VerifyOtpReq(@Email String email, @NotBlank String otp) {
}