package vn.host.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.host.entity.Payment;
import vn.host.model.request.PaymentRequest;

import java.io.IOException;

public interface PaymentService {
    String createVnPayRequest(PaymentRequest paymentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    <S extends Payment> S save(S entity);
}
