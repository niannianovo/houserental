package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.Notification;
import com.example.entity.RentPayment;
import com.example.entity.RentalOrder;
import com.example.mapper.RentPaymentMapper;
import com.example.mapper.RentalOrderMapper;
import com.example.service.NotificationService;
import com.example.service.RentPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class RentPaymentServiceImpl implements RentPaymentService {
    @Autowired
    private RentPaymentMapper rentPaymentMapper;
    @Autowired
    private RentalOrderMapper rentalOrderMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public List<RentPayment> getListByOrder(Integer orderId) {
        LambdaQueryWrapper<RentPayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentPayment::getOrderId, orderId)
               .orderByAsc(RentPayment::getPayMonth);
        return rentPaymentMapper.selectList(wrapper);
    }

    @Override
    public void pay(Integer id, Integer payMethod, String payProof) {
        RentPayment payment = rentPaymentMapper.selectById(id);
        if (payment == null) {
            throw new RuntimeException("缴费记录不存在");
        }
        if (payment.getStatus() == 1) {
            throw new RuntimeException("该月租金已缴纳");
        }

        payment.setStatus(1);           // 已缴
        payment.setPayMethod(payMethod);
        payment.setPayProof(payProof);
        payment.setPayTime(new Date());
        rentPaymentMapper.updateById(payment);

        log.info("【租金缴纳】缴费ID:{}, 租约ID:{}, 月份:{}", id, payment.getOrderId(), payment.getPayMonth());

        // 通知房东确认收款
        RentalOrder order = rentalOrderMapper.selectById(payment.getOrderId());
        if (order != null) {
            Notification notification = new Notification();
            notification.setUserId(order.getOwnerId());
            notification.setType(4);
            notification.setRelatedId(id);
            notification.setTitle("租金待确认");
            notification.setContent("租客已缴纳" + payment.getPayMonth() + "月租金 " + payment.getAmount() + " 元，请确认收款。");
            notificationService.send(notification);
        }
    }

    @Override
    public void confirm(Integer id, Integer ownerId) {
        RentPayment payment = rentPaymentMapper.selectById(id);
        if (payment == null) {
            throw new RuntimeException("缴费记录不存在");
        }
        if (payment.getStatus() != 1) {
            throw new RuntimeException("租客尚未缴费，无法确认");
        }
        if (payment.getConfirmStatus() == 1) {
            throw new RuntimeException("已确认过收款");
        }

        // 校验操作人是房东
        RentalOrder order = rentalOrderMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (!order.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("只有房东才能确认收款");
        }

        payment.setConfirmStatus(1); // 已确认
        rentPaymentMapper.updateById(payment);

        log.info("【确认收款】缴费ID:{}, 房东:{}, 月份:{}", id, ownerId, payment.getPayMonth());

        // 通知租客
        Notification notification = new Notification();
        notification.setUserId(order.getTenantId());
        notification.setType(4);
        notification.setRelatedId(id);
        notification.setTitle("收款已确认");
        notification.setContent("房东已确认收到您" + payment.getPayMonth() + "月的租金 " + payment.getAmount() + " 元。");
        notificationService.send(notification);
    }
}
