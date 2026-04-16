package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RentalOrder;

public interface RentalOrderService {
    void create(RentalOrder order);
    void sign(Integer id, Integer ownerId);
    void reject(Integer id, Integer ownerId);
    void payDeposit(Integer id, Integer tenantId);
    void refundDeposit(Integer id, Integer ownerId);
    void renewApply(Integer id, Integer tenantId, String newEndDate);
    void renewConfirm(Integer id, Integer ownerId);
    void quitApply(Integer id, Integer operatorId);
    void quitConfirm(Integer id, Integer operatorId);
    void quitCancel(Integer id, Integer operatorId);
    void cancel(Integer id, Integer operatorId);
    Page<RentalOrder> getMyList(Integer userId, Integer role, Integer page, Integer size);
    Page<RentalOrder> getHistory(Integer userId, Integer role, Integer page, Integer size);
}
