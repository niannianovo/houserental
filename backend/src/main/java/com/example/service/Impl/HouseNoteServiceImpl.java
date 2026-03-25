package com.example.service.Impl;

import com.example.entity.HouseNote;
import com.example.mapper.HouseNoteMapper;
import com.example.service.HouseNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HouseNoteServiceImpl implements HouseNoteService {
    @Autowired
    private HouseNoteMapper houseNoteMapper;

    @Override
    public void saveOrUpdate(HouseNote note) {
        // TODO 实现添加/更新备注
    }

    @Override
    public HouseNote getByUserAndHouse(Integer userId, Integer houseId) {
        // TODO 实现查询备注
        return null;
    }

    @Override
    public void delete(Integer userId, Integer houseId) {
        // TODO 实现删除备注
    }
}
