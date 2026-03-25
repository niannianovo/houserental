package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.HouseComment;

public interface HouseCommentService {
    void add(HouseComment comment);
    Page<HouseComment> getByHouse(Integer houseId, Integer page, Integer size);
    Page<HouseComment> getMyComments(Integer userId, Integer page, Integer size);
    void delete(Integer id, Integer adminId);
}
