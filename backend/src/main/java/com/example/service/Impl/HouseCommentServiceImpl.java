package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.HouseComment;
import com.example.entity.User;
import com.example.mapper.HouseCommentMapper;
import com.example.mapper.UserMapper;
import com.example.service.HouseCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HouseCommentServiceImpl implements HouseCommentService {
    @Autowired
    private HouseCommentMapper houseCommentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void add(HouseComment comment) {
        comment.setStatus(0); // 0=正常
        comment.setCreateTime(new Date());
        houseCommentMapper.insert(comment);
        log.info("【添加评论】用户:{} 对房源:{} 评分:{}", comment.getUserId(), comment.getHouseId(), comment.getRating());
    }

    @Override
    public Page<HouseComment> getByHouse(Integer houseId, Integer page, Integer size) {
        Page<HouseComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<HouseComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseComment::getHouseId, houseId)
               .eq(HouseComment::getStatus, 0)
               .orderByDesc(HouseComment::getCreateTime);
        Page<HouseComment> result = houseCommentMapper.selectPage(pageParam, wrapper);
        fillUserInfo(result.getRecords());
        return result;
    }

    @Override
    public Page<HouseComment> getMyComments(Integer userId, Integer page, Integer size) {
        Page<HouseComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<HouseComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseComment::getUserId, userId)
               .orderByDesc(HouseComment::getCreateTime);
        Page<HouseComment> result = houseCommentMapper.selectPage(pageParam, wrapper);
        fillUserInfo(result.getRecords());
        return result;
    }

    @Override
    public void delete(Integer id, Integer adminId) {
        HouseComment comment = houseCommentMapper.selectById(id);
        if (comment != null) {
            comment.setStatus(1); // 1=已删除
            houseCommentMapper.updateById(comment);
            log.info("【删除评论】管理员:{} 删除评论:{}", adminId, id);
        }
    }

    private void fillUserInfo(List<HouseComment> comments) {
        if (comments == null || comments.isEmpty()) return;
        Set<Integer> userIds = comments.stream().map(HouseComment::getUserId).collect(Collectors.toSet());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        for (HouseComment c : comments) {
            User u = userMap.get(c.getUserId());
            if (u != null) {
                c.setNickname(u.getNickname());
                c.setAvatar(u.getAvatar());
            }
        }
    }
}
