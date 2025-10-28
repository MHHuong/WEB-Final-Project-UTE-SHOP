package vn.host.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.host.entity.Payment;
import vn.host.model.request.PaymentRequest;
import vn.host.model.response.CreateMomoResponse;

import java.io.IOException;

public interface PaymentService {
    String createVnPayRequest(PaymentRequest paymentRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    CreateMomoResponse createMoMoRequest(PaymentRequest paymentRequest);

    <S extends Payment> S save(S entity);
}
