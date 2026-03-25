package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RentalOrder;
import com.example.mapper.RentalOrderMapper;
import com.example.service.RentalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RentalOrderServiceImpl implements RentalOrderService {
    @Autowired
    private RentalOrderMapper rentalOrderMapper;

    @Override
    public void create(RentalOrder order) {
        // TODO 实现创建租约
    }

    @Override
    public void sign(Integer id, Integer tenantId) {
        // TODO 实现签约
    }

    @Override
    public void renew(Integer id, String newEndDate) {
        // TODO 实现续租
    }

    @Override
    public void quitApply(Integer id, Integer operatorId) {
        // TODO 实现退租申请
    }

    @Override
    public void quitConfirm(Integer id, Integer operatorId) {
        // TODO 实现退租确认
    }

    @Override
    public Page<RentalOrder> getMyList(Integer userId, Integer page, Integer size) {
        // TODO 实现我的租约列表
        return null;
    }

    @Override
    public Page<RentalOrder> getHistory(Integer userId, Integer page, Integer size) {
        // TODO 实现租赁历史
        return null;
    }
}
