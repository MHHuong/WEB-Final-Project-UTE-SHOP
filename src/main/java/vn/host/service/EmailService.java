package vn.host.service;

import vn.host.entity.Order;
import vn.host.entity.User;
import vn.host.util.sharedenum.OrderStatus;

public interface EmailService {
    void sendOrderStatusEmail(Order order, User user, OrderStatus status);
}
