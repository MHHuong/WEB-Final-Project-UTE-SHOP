package vn.host.service;

public interface EmailService {
    void sendOtp(String to, String code);

    void sendText(String to, String subject, String body);
}