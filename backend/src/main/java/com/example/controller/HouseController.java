package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.House;
import com.example.service.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/house")
public class HouseController {
    @Autowired
    private HouseService houseService;

    @PostMapping
    public Result<String> publish(@RequestBody House house) {
        houseService.publish(house);
        return Result.success("发布成功");
    }

    @GetMapping("/list")
    public Result<Page<House>> search(@RequestParam(required = false) Integer id,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String address,
                                      @RequestParam(required = false) String province,
                                      @RequestParam(required = false) String city,
                                      @RequestParam(required = false) String district,
                                      @RequestParam(required = false) Integer houseType,
                                      @RequestParam(required = false) Integer minPrice,
                                      @RequestParam(required = false) Integer maxPrice,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) Integer verifyStatus,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(houseService.search(id, keyword, address, province, city, district, houseType, minPrice, maxPrice, status, verifyStatus, page, size));
    }

    @GetMapping("/{id}")
    public Result<House> getDetail(@PathVariable Integer id,
                                   @RequestParam(required = false) Integer userId) {
        return Result.success(houseService.getDetail(id, userId));
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody House house) {
        house.setId(id);
        houseService.update(house);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id, @RequestParam Integer ownerId) {
        houseService.delete(id, ownerId);
        return Result.success("删除成功");
    }

    @GetMapping("/my")
    public Result<Page<House>> getMyHouses(@RequestParam Integer ownerId,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(houseService.getMyHouses(ownerId, page, size));
    }
}
