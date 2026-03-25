package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.House;

public interface HouseService {
    void publish(House house);
    void update(House house);
    void delete(Integer id, Integer ownerId);
    House getDetail(Integer id, Integer userId);
    Page<House> search(String keyword, String address, Integer houseType,
                       Integer minPrice, Integer maxPrice, Integer page, Integer size);
    Page<House> getMyHouses(Integer ownerId, Integer page, Integer size);
}
