package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.VerifyRequest;

public interface VerifyRequestService {
    void submitIdentity(VerifyRequest request);
    void submitHouse(VerifyRequest request);
    Page<VerifyRequest> getMyRequests(Integer userId, Integer page, Integer size);
    Page<VerifyRequest> getPendingList(Integer page, Integer size);
    void audit(Integer id, Integer adminId, Integer status, String rejectReason);
}
