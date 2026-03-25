package com.example.service;

import com.example.entity.House;

import java.util.List;

public interface RecommendService {
    List<House> recommendForUser(Integer userId, Integer limit);
    List<House> getSimilar(Integer houseId, Integer limit);
}
