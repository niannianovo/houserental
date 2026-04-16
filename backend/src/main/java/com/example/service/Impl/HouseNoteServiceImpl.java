package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.HouseNote;
import com.example.mapper.HouseNoteMapper;
import com.example.service.HouseNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class HouseNoteServiceImpl implements HouseNoteService {
    @Autowired
    private HouseNoteMapper houseNoteMapper;

    @Override
    public void saveOrUpdate(HouseNote note) {
        HouseNote existing = getByUserAndHouse(note.getUserId(), note.getHouseId());
        if (existing != null) {
            // 更新已有备注
            existing.setContent(note.getContent());
            existing.setUpdateTime(new Date());
            houseNoteMapper.updateById(existing);
            log.info("【更新备注】用户:{} 房源:{}", note.getUserId(), note.getHouseId());
        } else {
            // 新增备注
            note.setCreateTime(new Date());
            note.setUpdateTime(new Date());
            houseNoteMapper.insert(note);
            log.info("【新增备注】用户:{} 房源:{}", note.getUserId(), note.getHouseId());
        }
    }

    @Override
    public HouseNote getByUserAndHouse(Integer userId, Integer houseId) {
        LambdaQueryWrapper<HouseNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseNote::getUserId, userId)
               .eq(HouseNote::getHouseId, houseId);
        return houseNoteMapper.selectOne(wrapper);
    }

    @Override
    public void delete(Integer userId, Integer houseId) {
        LambdaQueryWrapper<HouseNote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseNote::getUserId, userId)
               .eq(HouseNote::getHouseId, houseId);
        houseNoteMapper.delete(wrapper);
        log.info("【删除备注】用户:{} 房源:{}", userId, houseId);
    }
}
