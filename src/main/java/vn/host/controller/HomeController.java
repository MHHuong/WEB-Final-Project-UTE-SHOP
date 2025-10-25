package vn.host.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class HomeController {
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
        return "user/order/checkout";
    }
}
