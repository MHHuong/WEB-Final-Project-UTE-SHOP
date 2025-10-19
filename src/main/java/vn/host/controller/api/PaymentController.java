package vn.host.controller.api;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.host.entity.Order;
import vn.host.entity.Payment;
import vn.host.model.request.PaymentRequest;
import vn.host.model.response.ResponseModel;
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

    @GetMapping("/vn_pay/payment_info")
    public ResponseEntity<?> getPaymentInfoVnPay(
            @RequestParam("vnp_PayDate") String transDate,
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") String orderId,
            @RequestParam("vnp_TransactionNo") String transNo,
            @RequestParam("vnp_Amount") String amount,
            HttpServletResponse response
    ) throws IOException {
        try {
            Order order = orderService.findOrderById(Long.parseLong(orderId));
            Payment payment = Payment.builder()
                    .order(order)
                    .transactionCode(responseCode)
                    .amount(BigDecimal.valueOf(Long.parseLong(amount)))
                    .method(PaymentMethod.VNPAY)
                    .build();
            if ("00".equals(responseCode)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                orderService.updatePayment(Long.parseLong(orderId), payment);
                paymentService.save(payment);
                return new ResponseEntity<ResponseModel> (
                        new ResponseModel(
                                "Success",
                                "Thanh toán thành công",
                                responseCode
                        ), HttpStatus.OK
                );
            }
            else {
                payment.setStatus(PaymentStatus.FAILED);
                orderService.updatePayment(Long.parseLong(orderId), payment);
                paymentService.save(payment);
                return new ResponseEntity<ResponseModel> (
                        new ResponseModel(
                                "Failed",
                                "Thanh toán thất bại",
                                responseCode
                        ), HttpStatus.OK
                );
            }
        } catch (Exception e) {
            Order order = orderService.findOrderById(Long.parseLong(orderId));
            Payment payment = Payment.builder()
                    .order(order)
                    .transactionCode(responseCode)
                    .amount(BigDecimal.valueOf(Long.parseLong(amount)))
                    .status(PaymentStatus.FAILED)
                    .method(PaymentMethod.VNPAY)
                    .build();
            paymentService.save(payment);
            return new ResponseEntity<ResponseModel> (
                    new ResponseModel(
                        "Failed",
                        "Thanh toán thất bại",
                        e.getMessage()
                    ), HttpStatus.INTERNAL_SERVER_ERROR
            );
        }



        // Luôn cố gắng redirect về frontend, kể cả khi updatePaymentStatus gặp lỗi
                // thất bại
//        String redirectUrl;
//        try {
//            invoiceService.updatePaymentStatus(orderId, transNo, transDate, responseCode,null);
//
//            if ("00".equals(responseCode)) {
//                // thành công
//                redirectUrl = String.format(
//                        "%s/payment/result?orderId=%s&status=SUCCESS&transactionNo=%s&transactionDate=%s",
//                        frontendBaseUrl, orderId, transNo, transDate
//                );
//            } else {
//                // thất bại
//                redirectUrl = String.format(
//                        "%s/payment/result?orderId=%s&status=FAILED&responseCode=%s",
//                        frontendBaseUrl, orderId, responseCode
//                );
//            }
//        } catch (Exception e) {
//            String msg = e.getMessage() == null ? "error" : e.getMessage();
//            String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);
//            HoaDon hoaDon = invoiceService.getHoaDonByMaHD(orderId);
//            if (hoaDon != null && hoaDon.getResponseCode() != null && hoaDon.getResponseCode().equals("00")) {
//                redirectUrl = String.format(
//                        "%s/payment/result?orderId=%s&status=SUCCESS&transactionNo=%s&transactionDate=%s&message=%s",
//                        frontendBaseUrl, orderId, hoaDon.getTransactionNo(), hoaDon.getTransactionDate(), encoded
//                );
//            } else {
//                hoaDon.setTrangThai(InvoiceStatus.CANCELLED.getCode());
//                if (hoaDon.getVes() != null) {
//                    for (Ve ve : hoaDon.getVes()) {
//                        ve.setTrangThai(TicketStatus.CANCELLED.getCode());
//                    }
//                }
//                hoaDonRepository.save(hoaDon);
//                redirectUrl = String.format(
//                        "%s/payment/result?orderId=%s&status=FAILED&message=%s",
//                        frontendBaseUrl, orderId, encoded
//                );
//            }
//        }
//        try {
//            if (!response.isCommitted()) {
//                response.sendRedirect(redirectUrl);
//                return;
//            }
//        } catch (Exception ex) {
//            // nếu sendRedirect lỗi, sẽ gửi fallback HTML bên dưới
//            // log nếu cần
//
//            ex.printStackTrace();
//        }
//
//        // Fallback: nếu không thể redirect (response committed), ghi 1 trang HTML có meta refresh và link
//        try {
//            response.setContentType("text/html;charset=UTF-8");
//            String html = "<html><head><meta http-equiv=\"refresh\" content=\"0;url=" + redirectUrl + "\" /></head>"
//                    + "<body>Redirecting... If you are not redirected, <a href=\"" + redirectUrl + "\">click here</a>.</body></html>";
//            response.getWriter().write(html);
//            response.getWriter().flush();
//        } catch (IOException ignored) {
//            // nếu vẫn lỗi, không thể làm gì hơn
//        }
    }
}
