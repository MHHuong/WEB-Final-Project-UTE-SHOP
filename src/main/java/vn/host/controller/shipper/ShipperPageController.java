package vn.host.controller.shipper;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShipperPageController {
    @GetMapping("/shipper/register")
    public String registerPage() {
        return "shipper/register";
    }

    @GetMapping("/shipper/orders/confirmed")
    public String confirmedPage() {
        return "shipper/order-confirmed";
    }

    @GetMapping("/shipper/orders/shipping")
    public String shippingPage() {
        return "shipper/order-shipping";
    }

    @GetMapping("/shipper/history/picked")
    public String historyPickedPage() {
        return "shipper/history-picked";
    }

    @GetMapping("/shipper/history/delivered")
    public String historyDeliveredPage() {
        return "shipper/history-delivered";
    }
}
