package vn.host.controller.shipper;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('SHIPPER')")
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

    @GetMapping("/shipper")
    public String profilePage() {
        return "shipper/profile";
    }

    @GetMapping("/shipper/profile/edit")
    public String editProfilePage() {
        return "shipper/edit-profile";
    }

    @GetMapping("/shipper/orders/return/pickup")
    public String returnPickupPage() {
        return "shipper/order-return-pickup";
    }

    @GetMapping("/shipper/orders/return/deliver")
    public String returnDeliverPage() {
        return "shipper/order-return-deliver";
    }

    @GetMapping("/shipper/history/return/pickup")
    public String returnPickupHistoryPage() {
        return "shipper/history-return-pickup";
    }

    @GetMapping("/shipper/history/return/deliver")
    public String returnDeliverHistoryPage() {
        return "shipper/history-return-deliver";
    }
}
