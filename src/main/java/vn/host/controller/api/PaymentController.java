package vn.host.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Order;
import vn.host.entity.Payment;
import vn.host.model.request.PaymentRequest;
import vn.host.model.response.ResponseModel;
import vn.host.model.response.TempOrderResponse;
import vn.host.service.OrderService;
import vn.host.service.PaymentService;
import vn.host.service.impl.PaymentServiceImpl;
import vn.host.util.sharedenum.PaymentMethod;
import vn.host.util.sharedenum.PaymentStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @PostMapping("/vn_pay/create")
    public ResponseEntity<?> createPaymentVnPay(@RequestBody PaymentRequest paymentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            PaymentService paymentService = new PaymentServiceImpl();
            String paymentUrl = paymentService.createVnPayRequest(paymentRequest, request, response);
            return new ResponseEntity<ResponseModel>(
                    new ResponseModel(
                            "Success",
                            "Tạo thanh toán thành công",
                            paymentUrl
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<ResponseModel>(
                    new ResponseModel(
                            "Error",
                            "Tạo thanh toán thất bại: " + e.getMessage(),
                            null
                    ), HttpStatus.PAYMENT_REQUIRED
            );
        }
    }

    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomo(@RequestBody PaymentRequest paymentRequest) {
        try {
            return new ResponseEntity<>(
                    new ResponseModel(
                            "Success",
                            "Tạo thanh toán thành công",
                            paymentService.createMoMoRequest(paymentRequest)
                    ), HttpStatus.OK
            );
        }
        catch (Exception e) {
            return new ResponseEntity<>(
            new ResponseModel(
                    "Failed",
                    "Tạo thanh toán thất bại: " + e.getMessage(),
                    null
            ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
