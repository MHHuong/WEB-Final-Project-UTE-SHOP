package vn.host.controller.shop;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/shop")
    public String shopIndex() {
        return "shop/index";
    }

    @GetMapping("shop/order/order-list")
    public String orderList() {
        return "shop/order/order-list";
    }

    @GetMapping("shop/order/order-single")
    public String orderSingle() {
        return "shop/order/order-single";
    }

    @GetMapping("shop/product/products")
    public String products() {
        return "shop/product/products";
    }

    @GetMapping("shop/product/add-product")
    public String addProduct() {
        return "shop/product/add-product";
    }

    @GetMapping("shop/review/reviews")
    public String reviews() {
        return "shop/review/reviews";
    }

    @GetMapping("shop/account/profile")
    public String profile() {
        return "shop/account/profile";
    }

    @GetMapping("shop/promotion/promotions")
    public String promotions() {
        return "shop/promotion/promotions";
    }

    @GetMapping("/shop/register")
    public String registerPage() {
        return "shop/account/shop-register";
    }

    @GetMapping("/shop/product/edit-product")
    public String editProduct() {
        return "shop/product/edit-product";
    }
}
