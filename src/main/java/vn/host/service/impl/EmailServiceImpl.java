package vn.host.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.host.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mail;

    @Value("${app.otp.sender:UTE SHOP <no-reply@uteshop.vn>}")
    private String sender;

    @Override
    public void sendOtp(String to, String code) {
        sendText(to, "UTE SHOP - Mã xác thực (OTP)",
                "Mã OTP của bạn là: " + code + " (hiệu lực 5 phút).");
    }

    @Override
    public void sendText(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(sender);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mail.send(msg);
    }
}