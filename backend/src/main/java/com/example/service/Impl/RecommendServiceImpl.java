package com.example.service.Impl;

import com.example.entity.House;
import com.example.mapper.HouseMapper;
import com.example.mapper.UserPreferenceMapper;
import com.example.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RecommendServiceImpl implements RecommendService {
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @Override
    public List<House> recommendForUser(Integer userId, Integer limit) {
        // TODO 实现个性化推荐
        return null;
    }

    @Override
    public List<House> getSimilar(Integer houseId, Integer limit) {
        // TODO 实现相似推荐
        return null;
    }
}
