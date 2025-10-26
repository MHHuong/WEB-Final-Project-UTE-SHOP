package vn.host.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/status/{orderCode}")
    public String success(@PathVariable String orderCode, @RequestParam String status) {
        return "user/order/success";
    }
}
