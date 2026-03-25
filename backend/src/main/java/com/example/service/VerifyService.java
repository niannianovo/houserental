package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;

public interface VerifyService {
    int calculateScore(House house);
    Page<House> getPendingList(Integer page, Integer size);
    void audit(Integer houseId, Integer adminId, Integer action, String reason);
}
