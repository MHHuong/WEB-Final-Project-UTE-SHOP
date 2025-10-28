package vn.host.dto.auth;

import jakarta.validation.constraints.*;

public record LoginReq(@Email String email, @NotBlank String password) {
}