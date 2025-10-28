package vn.host.service;

public interface OtpService {
    String issue(String email);
    boolean verify(String email, String code);
    void invalidate(String email);
}