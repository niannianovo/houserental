package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.VerifyRequest;
import com.example.mapper.VerifyRequestMapper;
import com.example.service.VerifyRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerifyRequestServiceImpl implements VerifyRequestService {
    @Autowired
    private VerifyRequestMapper verifyRequestMapper;

    @Override
    public void submitIdentity(VerifyRequest request) {
        // TODO 实现提交实名认证
    }

    @Override
    public void submitHouse(VerifyRequest request) {
        // TODO 实现提交房源认证
    }

    @Override
    public Page<VerifyRequest> getMyRequests(Integer userId, Integer page, Integer size) {
        // TODO 实现我的认证申请列表
        return null;
    }

    @Override
    public Page<VerifyRequest> getPendingList(Integer page, Integer size) {
        // TODO 实现待审核列表
        return null;
    }

    @Override
    public void audit(Integer id, Integer adminId, Integer status, String rejectReason) {
        // TODO 实现审核认证
    }
}
