package vn.host.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.host.model.response.OrderResponse;
import vn.host.service.OrderService;

@Controller
@RequestMapping("/user")
public class HomeController {
    @Autowired
    OrderService orderService;

    @GetMapping()
    public String home() {
        return "user/home";
    }

    @GetMapping("/ws")
    public String WS() {
        return "user/test-ws";
    }

    @GetMapping("/shop-cart")
    public String shopCart() {
        return "user/order/shop-cart";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "user/order/check-out";
    }

    @GetMapping("/order/{orderCode}")
    public String success(@PathVariable String orderCode, @RequestParam String status) {
        return "user/order/order-detail";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile/profile";
    }

    @GetMapping("/order/detail")
    public String orders(@RequestParam Long orderId, Model model) {
        OrderResponse order = orderService.getOrderByOrderId(orderId);
        model.addAttribute("order", order);
        return "user/order/order-detail";
    }
}
