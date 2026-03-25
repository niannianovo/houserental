package com.example.controller;

import com.example.common.Result;
import com.example.entity.HouseNote;
import com.example.service.HouseNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/house-note")
public class HouseNoteController {
    @Autowired
    private HouseNoteService houseNoteService;

    @PostMapping("/{houseId}")
    public Result<String> saveOrUpdate(@PathVariable Integer houseId,
                                       @RequestParam Integer userId,
                                       @RequestParam String content) {
        HouseNote note = new HouseNote();
        note.setUserId(userId);
        note.setHouseId(houseId);
        note.setContent(content);
        houseNoteService.saveOrUpdate(note);
        return Result.success("保存成功");
    }

    @GetMapping("/{houseId}")
    public Result<HouseNote> get(@PathVariable Integer houseId, @RequestParam Integer userId) {
        return Result.success(houseNoteService.getByUserAndHouse(userId, houseId));
    }

    @DeleteMapping("/{houseId}")
    public Result<String> delete(@PathVariable Integer houseId, @RequestParam Integer userId) {
        houseNoteService.delete(userId, houseId);
        return Result.success("删除成功");
    }
}
