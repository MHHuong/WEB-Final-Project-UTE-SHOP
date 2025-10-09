package vn.host.service;

import vn.host.entity.Payment;

import java.util.List;

public interface PaymentService {
    void save(Payment payment);
    void delete(long id);
    List<Payment> findAll();
    Payment findById(long id);
}
