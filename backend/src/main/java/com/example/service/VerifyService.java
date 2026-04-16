package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;

public interface VerifyService {
    Page<House> getPendingList(Integer page, Integer size);

    void audit(Integer houseId, Integer adminId, Integer action, String reason);
}
