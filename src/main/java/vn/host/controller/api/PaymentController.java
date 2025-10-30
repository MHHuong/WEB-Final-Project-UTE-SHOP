package vn.host.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.model.request.PaymentRequest;
import vn.host.model.response.ApiResponse;
import vn.host.service.OrderService;
import vn.host.service.PaymentService;
import vn.host.service.impl.PaymentServiceImpl;

import java.io.IOException;

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
            System.out.print(paymentUrl);
            return new ResponseEntity<ApiResponse>(
                    new ApiResponse(
                            "Success",
                            "Create payment successfully",
                            paymentUrl
                    ), HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<ApiResponse>(
                    new ApiResponse(
                            "Error",
                            "Create payment failed: " + e.getMessage(),
                            null
                    ), HttpStatus.PAYMENT_REQUIRED
            );
        }
    }

    @PostMapping("/momo/create")
    public ResponseEntity<?> createMomo(@RequestBody PaymentRequest paymentRequest) {
        try {
            return new ResponseEntity<>(
                    new ApiResponse(
                            "Success",
                            "Create MoMo payment successfully",
                            paymentService.createMoMoRequest(paymentRequest)
                    ), HttpStatus.OK
            );
        }
        catch (Exception e) {
            return new ResponseEntity<>(
            new ApiResponse(
                    "Error",
                    "Create payment failed: " + e.getMessage(),
                    null
            ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
