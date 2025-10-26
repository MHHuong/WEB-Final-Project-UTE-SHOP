package vn.host.controller;


import jakarta.servlet.http.HttpSession;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.host.service.OrderService;

import java.io.IOException;

@Controller
@RequestMapping("/payment")
public class PaymentInfoController {
    @Autowired
    OrderService orderService;

    @GetMapping("/vn_pay/payment_info")
    public String getPaymentInfoVnPay(
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") String orderId,
            @RequestParam("vnp_Amount") Long amount
    ) throws IOException {
        try {
            orderService.updateOrderPaymentVnPay(orderId, responseCode, amount);
            return "redirect:/status/" + orderId + "?status=success";
        } catch (Exception e) {
            return "redirect:/status/" + orderId + "?status=failed";
        }
    }

    @GetMapping("/momo/payment_info")
    public String getPaymentInfoMomo(
            @RequestParam("amount") Long amount,
            @RequestParam("resultCode") Integer resultCode,
            @RequestParam("orderId") String orderId
    ) throws IOException {
        try {
            orderService.updateOrderPaymentMomo(orderId, resultCode, amount);
            return "redirect:/status/" + orderId + "?status=success";
        } catch (Exception e) {
            return "redirect:/status/" + orderId + "?status=failed";
        }
    }
}
