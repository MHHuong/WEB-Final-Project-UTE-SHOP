package vn.host.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import vn.host.entity.Order;
import vn.host.entity.User;
import vn.host.service.EmailService;
import vn.host.util.sharedenum.OrderStatus;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mail;
    private final SpringTemplateEngine templateEngine;

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

    @Override
    public void sendOrderStatusEmail(Order order, User user, OrderStatus status) {
        Context context = new Context();
        String to = user.getEmail();
        context.setVariable("customerName", user.getFullName());
        context.setVariable("orderId", order.getOrderId());
        context.setVariable("status", getEmailSubject(status, order.getOrderId()));
        context.setVariable("totalAmount", order.getTotalAmount());
        context.setVariable("orderUrl", "http://localhost:8082/user/order/" + order.getOrderId());

        String htmlContent = templateEngine.process("email/status", context);
        MimeMessage message = mail.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(getEmailSubject(status, order.getOrderId()));
            helper.setText(htmlContent, true);
            mail.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getEmailSubject(OrderStatus status, Long orderId) {
        return switch (status) {
            case NEW -> "Đơn hàng #" + orderId + " đã được tạo thành công";
            case CONFIRMED -> "Đơn hàng #" + orderId + " đã được xác nhận";
            case SHIPPING -> "Đơn hàng #" + orderId + " đang trên đường giao đến bạn";
            case DELIVERED -> "Đơn hàng #" + orderId + " đã được giao thành công";
            case RECEIVED -> "Cảm ơn bạn đã xác nhận đơn hàng #" + orderId;
            case CANCELLED -> "Đơn hàng #" + orderId + " đã bị hủy";
            case RETURNED -> "Đơn hàng #" + orderId + " đã được trả lại";
        };
    }
}
