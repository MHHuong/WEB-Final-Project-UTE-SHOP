package vn.host.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterReq(@Email String email, @NotBlank String password, @NotBlank String fullName) {
}