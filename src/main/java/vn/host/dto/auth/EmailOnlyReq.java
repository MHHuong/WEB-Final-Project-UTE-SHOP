package vn.host.dto.auth;

import jakarta.validation.constraints.Email;

public record EmailOnlyReq(@Email String email) {
}