package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.Appointment;
import com.example.entity.House;
import com.example.entity.Notification;
import com.example.mapper.AppointmentMapper;
import com.example.mapper.HouseMapper;
import com.example.service.AppointmentService;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void create(Appointment appointment) {
        // 校验
        if (appointment.getHouseId() == null) {
            throw new RuntimeException("房源ID不能为空");
        }
        if (appointment.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new RuntimeException("预约时间不能为空");
        }
        if (appointment.getAppointmentTime().before(new Date())) {
            throw new RuntimeException("预约时间不能早于当前时间");
        }

        House house = houseMapper.selectById(appointment.getHouseId());
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }

        // 检查是否已有待处理的预约
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getHouseId, appointment.getHouseId())
               .eq(Appointment::getUserId, appointment.getUserId())
               .eq(Appointment::getStatus, 0);
        if (appointmentMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已有该房源的待处理预约");
        }

        appointment.setStatus(0);
        appointment.setCreateTime(new Date());
        appointmentMapper.insert(appointment);

        // 通知房东
        Notification notification = new Notification();
        notification.setUserId(house.getOwnerId());
        notification.setType(5);
        notification.setTitle("新的看房预约");
        notification.setContent("有租客预约查看您的房源「" + house.getTitle() + "」");
        notification.setRelatedId(appointment.getId());
        notificationService.send(notification);

        log.info("【预约创建】用户:{} 预约房源:{}", appointment.getUserId(), appointment.getHouseId());
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 状态校验：0待处理 -> 1同意/2拒绝/4取消, 1已同意 -> 3已完成
        int current = appointment.getStatus();
        if (current == 0 && (status == 1 || status == 2 || status == 4)) {
            // ok
        } else if (current == 1 && status == 3) {
            // ok
        } else {
            throw new RuntimeException("当前状态不允许该操作");
        }

        appointment.setStatus(status);
        appointmentMapper.updateById(appointment);

        // 通知预约人状态变更
        String msg;
        if (status == 1) msg = "您的看房预约已被同意";
        else if (status == 2) msg = "您的看房预约已被拒绝";
        else if (status == 3) msg = "您的看房预约已标记为完成";
        else msg = "您的看房预约已取消";

        Notification notification = new Notification();
        notification.setUserId(appointment.getUserId());
        notification.setType(5);
        notification.setTitle("预约状态更新");
        notification.setContent(msg);
        notification.setRelatedId(id);
        notificationService.send(notification);

        log.info("【预约状态更新】ID:{} 状态:{}->{}", id, current, status);
    }

    @Override
    public Page<Appointment> getMyList(Integer userId, Integer page, Integer size) {
        Page<Appointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getUserId, userId)
               .orderByDesc(Appointment::getCreateTime);
        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        fillHouseInfo(result.getRecords());
        return result;
    }

    /** 批量回填 houseTitle / houseStatus；已删除的房源两个字段保持 null，前端按"已下架"处理 */
    private void fillHouseInfo(List<Appointment> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Integer> ids = list.stream()
                .map(Appointment::getHouseId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Integer, House> map = new HashMap<>();
        for (House h : houseMapper.selectBatchIds(ids)) {
            map.put(h.getId(), h);
        }
        for (Appointment a : list) {
            House h = map.get(a.getHouseId());
            if (h != null) {
                a.setHouseTitle(h.getTitle());
                a.setHouseStatus(h.getStatus());
            }
        }
    }

    @Override
    public Page<Appointment> getLandlordList(Integer ownerId, Integer page, Integer size) {
        // 先查出房东的所有房源ID
        LambdaQueryWrapper<House> houseWrapper = new LambdaQueryWrapper<>();
        houseWrapper.eq(House::getOwnerId, ownerId).select(House::getId);
        List<Integer> houseIds = houseMapper.selectList(houseWrapper)
                .stream().map(House::getId).collect(Collectors.toList());

        Page<Appointment> pageParam = new Page<>(page, size);
        if (houseIds.isEmpty()) {
            return pageParam;
        }

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Appointment::getHouseId, houseIds)
               .orderByDesc(Appointment::getCreateTime);
        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        fillHouseInfo(result.getRecords());
        return result;
    }
}
