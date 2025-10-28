package vn.host.service;

import vn.host.entity.Order;
import vn.host.entity.User;
import vn.host.util.sharedenum.OrderStatus;

public interface EmailService {
    void sendOtp(String to, String code);

    void sendText(String to, String subject, String body);

    void sendOrderStatusEmail(Order order, User user, OrderStatus status);
}