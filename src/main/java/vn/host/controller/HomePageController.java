package vn.host.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/user")
public class HomePageController {
    @GetMapping()
    public String home(Model model) {
        return"user/home";
    }

    @GetMapping("/ws")
    public String WS(Model model) {
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
    public String success(@PathVariable String orderCode, @RequestParam String status, Model model) {
        return "user/order/order-detail";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        return "user/profile/profile";
    }

    @GetMapping("/order/detail")
    public String orders(@RequestParam Long orderId) {
        return "user/order/order-detail";
    }
}
