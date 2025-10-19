package vn.host.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.model.PaymentRequest;
import vn.host.model.ResponseModel;
import vn.host.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/vn_pay/create")
    public ResponseEntity<?> createPaymentVnPay(@RequestBody PaymentRequest paymentRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            String paymentUrl = paymentService.createVnPayRequest(paymentRequest, request, response);
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Tạo thanh toán thành công",
                            paymentUrl
                    ), HttpStatus.CREATED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Tạo thanh toán thất bại: " + e.getMessage(),
                            null
                    ), HttpStatus.PAYMENT_REQUIRED
            );
        }
    }

    @GetMapping("/vn_pay/payment_info")
    public ResponseEntity<?> getPaymentInfoVnPay(
            @RequestParam("vnp_TransactionNo") String transNo,
            @RequestParam("vnp_PayDate") String transDate,
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") String orderId
    ) {
        try {
            Map<String, String> info = Map.of(
                    "transactionNo", transNo,
                    "transactionDate", transDate,
                    "responseCode", responseCode,
                    "orderId", orderId
            );

            if ("00".equals(responseCode)) {
                return new ResponseEntity<>(
                        new ResponseModel(
                                "Success",
                                "Thanh toán thành công",
                                info
                        ), HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                        new ResponseModel(
                                "Failed",
                                "Thanh toán thất bại",
                                info
                        ), HttpStatus.BAD_REQUEST
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Error",
                            "Xử lý thông tin thanh toán thất bại: " + e.getMessage(),
                            null
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
