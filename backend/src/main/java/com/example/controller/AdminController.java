package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Result;
import com.example.entity.House;
import com.example.entity.Notification;
import com.example.entity.RentalOrder;
import com.example.entity.User;
import com.example.mapper.HouseMapper;
import com.example.mapper.RentalOrderMapper;
import com.example.mapper.UserMapper;
import com.example.service.HouseCommentService;
import com.example.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private HouseCommentService houseCommentService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private RentalOrderMapper rentalOrderMapper;
    @Autowired
    private NotificationService notificationService;

    // 用户列表
    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsAdmin, 0); // 只查普通用户
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PutMapping("/user/disable/{id}")
    public Result<String> disableUser(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new RuntimeException("该用户已被禁用");
        }

        // 1. 存在进行中/续租中/退租中的租约则拒绝禁用
        LambdaQueryWrapper<RentalOrder> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.and(w -> w.eq(RentalOrder::getTenantId, id).or().eq(RentalOrder::getOwnerId, id))
                     .in(RentalOrder::getStatus, Arrays.asList(1, 3, 4));
        Long activeCount = rentalOrderMapper.selectCount(activeWrapper);
        if (activeCount != null && activeCount > 0) {
            throw new RuntimeException("该用户存在 " + activeCount + " 个进行中的租约，请先协调退租后再禁用");
        }

        // 2. 取消该用户待签约/待付押金的订单，通知对方
        LambdaQueryWrapper<RentalOrder> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.and(w -> w.eq(RentalOrder::getTenantId, id).or().eq(RentalOrder::getOwnerId, id))
                      .in(RentalOrder::getStatus, Arrays.asList(0, 6));
        List<RentalOrder> pendingOrders = rentalOrderMapper.selectList(pendingWrapper);
        for (RentalOrder order : pendingOrders) {
            if(order.getStatus() == 0) {
                order.setStatus(7); // 已拒绝
            }else if (order.getStatus() == 6){
                order.setStatus(8); // 已取消
            }
            rentalOrderMapper.updateById(order);
            Integer targetId = id.equals(order.getTenantId()) ? order.getOwnerId() : order.getTenantId();
            String msg = "由于对方账号已被管理员禁用，租约(ID:" + order.getId() + ")已自动取消。";
            sendNotification(targetId, "订单取消通知", msg, 5, order.getId());
        }

        // 3. 下架该用户名下所有在售房源
        LambdaQueryWrapper<House> houseWrapper = new LambdaQueryWrapper<>();
        houseWrapper.eq(House::getOwnerId, id).eq(House::getStatus, 1);
        List<House> listedHouses = houseMapper.selectList(houseWrapper);
        for (House h : listedHouses) {
            h.setStatus(2); // 下架
            h.setUpdateTime(new Date());
            houseMapper.updateById(h);
        }

        // 4. 禁用账号
        user.setStatus(1);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        log.info("【禁用用户】用户ID:{}, 取消订单:{}, 下架房源:{}", id, pendingOrders.size(), listedHouses.size());

        String result = "已禁用";
        if (!pendingOrders.isEmpty() || !listedHouses.isEmpty()) {
            result += "（同时取消了 " + pendingOrders.size() + " 个待处理订单，下架了 " + listedHouses.size() + " 个在售房源）";
        }
        return Result.success(result);
    }

    @PutMapping("/user/enable/{id}")
    public Result<String> enableUser(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setStatus(0); // 0正常
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        log.info("【启用用户】用户ID:{}", id);
        return Result.success("已启用");
    }

//    @DeleteMapping("/user/{id}")
//    public Result<String> deleteUser(@PathVariable Integer id) {
//        userMapper.deleteById(id);
//        log.info("【删除用户】用户ID:{}", id);
//        return Result.success("已删除");
//    }

    @DeleteMapping("/comment/{id}")
    public Result<String> deleteComment(@PathVariable Integer id, @RequestParam Integer adminId) {
        houseCommentService.delete(id, adminId);
        return Result.success("评论已删除");
    }

    // 管理员强制下架房源
    @PutMapping("/house/takedown/{id}")
    public Result<String> takedownHouse(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        House house = houseMapper.selectById(id);
        if (house == null) throw new RuntimeException("房源不存在");
        if (house.getStatus() == 2) throw new RuntimeException("该房源已处于下架状态");

        // 查询该房源的活跃订单
        List<RentalOrder> activeOrders = getActiveOrders(id);

        // 有进行中/续租中/退租中的订单 → 不允许下架，提示走退租流程
        for (RentalOrder order : activeOrders) {
            if (order.getStatus() == 1 || order.getStatus() == 3 || order.getStatus() == 4) {
                throw new RuntimeException("该房源存在进行中的租约（订单ID: " + order.getId() + "），无法直接下架，请联系租客和房东协商退租");
            }
        }

        // 自动取消待签约(0)和待付押金(6)的订单
        cancelPendingOrders(activeOrders, house.getTitle(), reason);

        house.setStatus(2);
        house.setUpdateTime(new Date());
        houseMapper.updateById(house);
        log.info("【管理员下架房源】房源ID:{}, 原因:{}", id, reason);

        // 通知房东
        String msg = "您的房源「" + house.getTitle() + "」已被管理员强制下架";
        if (reason != null && !reason.isEmpty()) {
            msg += "，原因：" + reason;
        }
        sendNotification(house.getOwnerId(), "房源下架通知", msg, 4, id);
        return Result.success("已下架");
    }

    // 管理员删除房源
    @DeleteMapping("/house/{id}")
    public Result<String> deleteHouse(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        House house = houseMapper.selectById(id);
        if (house == null) throw new RuntimeException("房源不存在");

        // 查询该房源的活跃订单
        List<RentalOrder> activeOrders = getActiveOrders(id);

        // 有进行中/续租中/退租中的订单 → 不允许删除
        for (RentalOrder order : activeOrders) {
            if (order.getStatus() == 1 || order.getStatus() == 3 || order.getStatus() == 4) {
                throw new RuntimeException("该房源存在进行中的租约（订单ID: " + order.getId() + "），无法删除，请等待租约结束");
            }
        }

        // 自动取消待签约(0)和待付押金(6)的订单
        cancelPendingOrders(activeOrders, house.getTitle(), reason);

        houseMapper.deleteById(id);
        log.info("【管理员删除房源】房源ID:{}, 原因:{}", id, reason);

        // 通知房东
        String msg = "您的房源「" + house.getTitle() + "」已被管理员删除";
        if (reason != null && !reason.isEmpty()) {
            msg += "，原因：" + reason;
        }
        sendNotification(house.getOwnerId(), "房源删除通知", msg, 4, null);
        return Result.success("已删除");
    }

    /**
     * 查询房源的活跃订单（待签约、进行中、续租中、退租中、待付押金）
     */
    private List<RentalOrder> getActiveOrders(Integer houseId) {
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentalOrder::getHouseId, houseId)
               .in(RentalOrder::getStatus, Arrays.asList(0, 1, 3, 4, 6));
        return rentalOrderMapper.selectList(wrapper);
    }

    /**
     * 自动取消待签约(0)和待付押金(6)的订单，并通知租客
     */
    private void cancelPendingOrders(List<RentalOrder> orders, String houseTitle, String reason) {
        for (RentalOrder order : orders) {
            if (order.getStatus() == 0 || order.getStatus() == 6) {
                order.setStatus(7); // 设为已拒绝
                rentalOrderMapper.updateById(order);
                log.info("【管理员操作-自动取消订单】订单ID:{}", order.getId());

                String msg = "您申请的房源「" + houseTitle + "」已被管理员处理，相关订单已自动取消";
                if (reason != null && !reason.isEmpty()) {
                    msg += "，原因：" + reason;
                }
                sendNotification(order.getTenantId(), "订单取消通知", msg, 5, order.getId());
            }
        }
    }

    private void sendNotification(Integer userId, String title, String content, Integer type, Integer relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        notificationService.send(n);
    }
}
