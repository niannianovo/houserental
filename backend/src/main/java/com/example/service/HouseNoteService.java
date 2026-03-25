package com.example.service;

import com.example.entity.HouseNote;

public interface HouseNoteService {
    void saveOrUpdate(HouseNote note);
    HouseNote getByUserAndHouse(Integer userId, Integer houseId);
    void delete(Integer userId, Integer houseId);
}
