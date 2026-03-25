package com.example.service.Impl;

import com.example.entity.RentPayment;
import com.example.mapper.RentPaymentMapper;
import com.example.service.RentPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RentPaymentServiceImpl implements RentPaymentService {
    @Autowired
    private RentPaymentMapper rentPaymentMapper;

    @Override
    public List<RentPayment> getListByOrder(Integer orderId) {
        // TODO 实现缴费记录列表
        return null;
    }

    @Override
    public void pay(Integer id, Integer payMethod, String payProof) {
        // TODO 实现支付
    }

    @Override
    public void confirm(Integer id, Integer ownerId) {
        // TODO 实现确认收款
    }
}
