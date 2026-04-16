package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.QuickReply;
import com.example.mapper.QuickReplyMapper;
import com.example.service.QuickReplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class QuickReplyServiceImpl implements QuickReplyService {
    @Autowired
    private QuickReplyMapper quickReplyMapper;

    @Override
    public List<QuickReply> getList(Integer userId) {
        LambdaQueryWrapper<QuickReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuickReply::getUserId, userId)
               .orderByAsc(QuickReply::getSortOrder)
               .orderByDesc(QuickReply::getCreateTime);
        return quickReplyMapper.selectList(wrapper);
    }

    @Override
    public void add(QuickReply quickReply) {
        quickReply.setCreateTime(new Date());
        quickReplyMapper.insert(quickReply);
        log.info("【新增快捷回复】用户:{} 标题:{}", quickReply.getUserId(), quickReply.getTitle());
    }

    @Override
    public void update(QuickReply quickReply) {
        QuickReply existing = quickReplyMapper.selectById(quickReply.getId());
        if (existing == null) {
            throw new RuntimeException("快捷回复不存在");
        }
        if (!existing.getUserId().equals(quickReply.getUserId())) {
            throw new RuntimeException("无权修改他人的快捷回复");
        }
        quickReplyMapper.updateById(quickReply);
        log.info("【修改快捷回复】用户:{} id:{}", quickReply.getUserId(), quickReply.getId());
    }

    @Override
    public void delete(Integer id, Integer userId) {
        QuickReply existing = quickReplyMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("快捷回复不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的快捷回复");
        }
        quickReplyMapper.deleteById(id);
        log.info("【删除快捷回复】用户:{} id:{}", userId, id);
    }
}
