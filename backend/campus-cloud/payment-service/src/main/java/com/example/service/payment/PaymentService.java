package com.example.service.payment;

import com.example.dto.payment.PaymentCreateDTO;
import com.example.entity.Payment;

public interface PaymentService {

    Long createPayment(PaymentCreateDTO dto);

    void pay(Long id);

    Payment getPayment(Long id);
}
