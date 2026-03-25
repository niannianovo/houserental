package com.example.service;

import com.example.entity.RentPayment;

import java.util.List;

public interface RentPaymentService {
    List<RentPayment> getListByOrder(Integer orderId);
    void pay(Integer id, Integer payMethod, String payProof);
    void confirm(Integer id, Integer ownerId);
}
