package vn.host.dto.auth;

public record AuthRes(String token, String email, String fullName, String role, Long userId) {}