package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RentalOrder;

public interface RentalOrderService {
    void create(RentalOrder order);
    void sign(Integer id, Integer tenantId);
    void renew(Integer id, String newEndDate);
    void quitApply(Integer id, Integer operatorId);
    void quitConfirm(Integer id, Integer operatorId);
    Page<RentalOrder> getMyList(Integer userId, Integer page, Integer size);
    Page<RentalOrder> getHistory(Integer userId, Integer page, Integer size);
}
