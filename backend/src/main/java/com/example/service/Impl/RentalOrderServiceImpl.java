package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.*;
import com.example.mapper.HouseMapper;
import com.example.mapper.RentalOrderLogMapper;
import com.example.mapper.RentalOrderMapper;
import com.example.mapper.RentPaymentMapper;
import com.example.service.NotificationService;
import com.example.service.RentalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RentalOrderServiceImpl implements RentalOrderService {
    @Autowired
    private RentalOrderMapper rentalOrderMapper;
    @Autowired
    private RentalOrderLogMapper rentalOrderLogMapper;
    @Autowired
    private RentPaymentMapper rentPaymentMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void create(RentalOrder order) {
        if (order.getHouseId() == null || order.getTenantId() == null || order.getOwnerId() == null) {
            throw new RuntimeException("房源ID、租客ID、房东ID不能为空");
        }
        if (order.getStartDate() == null || order.getEndDate() == null) {
            throw new RuntimeException("租期起止日期不能为空");
        }
        if (order.getTenantId().equals(order.getOwnerId())) {
            throw new RuntimeException("不能租自己的房源");
        }

        House house = houseMapper.selectById(order.getHouseId());
        if (house == null) {
            throw new RuntimeException("房源不存在");
        }
        if (house.getVerifyStatus() == null || house.getVerifyStatus() != 1) {
            throw new RuntimeException("房源未通过审核，无法签约");
        }
        if (house.getStatus() != null && house.getStatus() == 3) {
            throw new RuntimeException("房源已出租");
        }

        // 起租日不能早于今天
        Date today = new Date();
        today = truncateToDay(today);
        if (order.getStartDate().before(today)) {
            throw new RuntimeException("起租日不能早于今天");
        }

        // 设置月租（以房源当前价格为准）
        order.setMonthlyRent(house.getPrice());
        order.setStatus(0); // 待签约
        order.setCreateTime(new Date());
        rentalOrderMapper.insert(order);

        // 记录操作日志
        addLog(order.getId(), order.getTenantId(), "创建租约", null);
        log.info("【创建租约】租约ID:{}, 房源:{}, 租客:{}, 房东:{}", order.getId(), order.getHouseId(), order.getTenantId(), order.getOwnerId());

        // 通知房东
        Notification notification = new Notification();
        notification.setUserId(order.getOwnerId());
        notification.setType(5);
        notification.setRelatedId(order.getId());
        notification.setTitle("新的签约申请");
        notification.setContent("租客(ID:" + order.getTenantId() + ")申请签约您的房源【" + house.getTitle() + "】，请及时确认。");
        notificationService.send(notification);
    }

    @Override
    public void sign(Integer id, Integer ownerId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前状态无法签约");
        }

        // 检查该房源是否已有生效的租约（状态1进行中、3续租中、4退租申请中、6待付押金）
        LambdaQueryWrapper<RentalOrder> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(RentalOrder::getHouseId, order.getHouseId())
                     .in(RentalOrder::getStatus, 1, 3, 4, 6);
        if (rentalOrderMapper.selectCount(activeWrapper) > 0) {
            throw new RuntimeException("该房源已有生效的租约，无法再签约");
        }

        // 获取房源押金
        House house = houseMapper.selectById(order.getHouseId());
        BigDecimal deposit = (house != null && house.getDeposit() != null) ? house.getDeposit() : BigDecimal.ZERO;

        // 签约 → 状态改为待付押金
        order.setStatus(6);
        order.setDepositAmount(deposit);
        order.setDepositStatus(0); // 未付
        rentalOrderMapper.updateById(order);

        addLog(id, ownerId, "签约", "押金：" + deposit + "元，等待租客缴纳");
        log.info("【签约成功，待付押金】租约ID:{}, 押金:{}", id, deposit);

        // 通知租客去付押金
        Notification notification = new Notification();
        notification.setUserId(order.getTenantId());
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("签约成功，请缴纳押金");
        notification.setContent("房东已同意签约，请缴纳押金 " + deposit + " 元后租约正式生效。租期：" + formatDate(order.getStartDate()) + " 至 " + formatDate(order.getEndDate()));
        notificationService.send(notification);
    }

    @Override
    public void reject(Integer id, Integer ownerId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前状态无法拒绝");
        }

        order.setStatus(7); // 已拒绝
        rentalOrderMapper.updateById(order);

        addLog(id, ownerId, "拒绝签约", null);
        log.info("【拒绝签约】租约ID:{}", id);

        // 通知租客
        Notification notification = new Notification();
        notification.setUserId(order.getTenantId());
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("签约申请被拒绝");
        notification.setContent("房东拒绝了您的签约申请（租约ID:" + id + "）。");
        notificationService.send(notification);
    }

    @Override
    public void payDeposit(Integer id, Integer tenantId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 6) {
            throw new RuntimeException("当前状态无需缴纳押金");
        }
        if (!order.getTenantId().equals(tenantId)) {
            throw new RuntimeException("只有租客才能缴纳押金");
        }

        // 押金已付，订单正式开始
        order.setStatus(1);
        order.setDepositStatus(1); // 已付
        rentalOrderMapper.updateById(order);

        // 房源状态改为已出租
        House house = houseMapper.selectById(order.getHouseId());
        if (house != null) {
            house.setStatus(3);
            house.setUpdateTime(new Date());
            houseMapper.updateById(house);
        }

        // 生成每月缴费记录
        generatePayments(order);

        addLog(id, tenantId, "缴纳押金", "押金 " + order.getDepositAmount() + " 元");
        log.info("【押金已付】租约ID:{}, 金额:{}", id, order.getDepositAmount());

        // 通知房东
        Notification notification = new Notification();
        notification.setUserId(order.getOwnerId());
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("租客已缴纳押金");
        notification.setContent("租客(ID:" + tenantId + ")已缴纳押金 " + order.getDepositAmount() + " 元，租约正式生效。");
        notificationService.send(notification);
    }

    @Override
    public void refundDeposit(Integer id, Integer ownerId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 5) {
            throw new RuntimeException("只有已退租的租约才能退押金");
        }
        if (!order.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("只有房东才能退押金");
        }
        if (order.getDepositStatus() == null || order.getDepositStatus() != 1) {
            throw new RuntimeException("押金未缴纳或已退还");
        }

        order.setDepositStatus(2); // 已退
        rentalOrderMapper.updateById(order);

        addLog(id, ownerId, "退还押金", "退还 " + order.getDepositAmount() + " 元");
        log.info("【押金已退】租约ID:{}, 金额:{}", id, order.getDepositAmount());

        // 通知租客
        Notification notification = new Notification();
        notification.setUserId(order.getTenantId());
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("押金已退还");
        notification.setContent("房东已退还押金 " + order.getDepositAmount() + " 元。");
        notificationService.send(notification);
    }

    @Override
    public void renewApply(Integer id, Integer tenantId, String newEndDate) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("只有进行中的租约才能申请续租");
        }
        if (!order.getTenantId().equals(tenantId)) {
            throw new RuntimeException("只有租客才能申请续租");
        }

        Date newEnd;
        try {
            newEnd = new SimpleDateFormat("yyyy-MM-dd").parse(newEndDate);
        } catch (ParseException e) {
            throw new RuntimeException("日期格式错误，请使用yyyy-MM-dd");
        }
        if (!newEnd.after(order.getEndDate())) {
            throw new RuntimeException("新到期日必须晚于原到期日");
        }

        order.setStatus(3); // 续租申请中
        rentalOrderMapper.updateById(order);

        addLog(id, tenantId, "续租申请", "申请续租至" + newEndDate);
        log.info("【续租申请】租约ID:{}, 租客:{}, 新到期日:{}", id, tenantId, newEndDate);

        // 通知房东
        Notification notification = new Notification();
        notification.setUserId(order.getOwnerId());
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("续租申请");
        notification.setContent("租客(ID:" + tenantId + ")申请将租约续租至 " + newEndDate + "，请确认。");
        notificationService.send(notification);
    }

    @Override
    public void renewConfirm(Integer id, Integer ownerId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("当前状态无法确认续租");
        }
        if (!order.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("只有房东才能确认续租");
        }

        // 从最近的续租申请日志中获取新到期日
        LambdaQueryWrapper<RentalOrderLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(RentalOrderLog::getOrderId, id)
                  .eq(RentalOrderLog::getAction, "续租申请")
                  .orderByDesc(RentalOrderLog::getCreateTime)
                  .last("LIMIT 1");
        RentalOrderLog renewLog = rentalOrderLogMapper.selectOne(logWrapper);
        if (renewLog == null || renewLog.getRemark() == null) {
            throw new RuntimeException("未找到续租申请记录");
        }

        String newEndDate = renewLog.getRemark().replace("申请续租至", "");
        Date newEnd;
        try {
            newEnd = new SimpleDateFormat("yyyy-MM-dd").parse(newEndDate);
        } catch (ParseException e) {
            throw new RuntimeException("续租日期解析失败");
        }

        Date oldEnd = order.getEndDate();
        order.setEndDate(newEnd);
        order.setStatus(1); // 恢复进行中
        rentalOrderMapper.updateById(order);

        // 为续租区间生成新的缴费记录
        generatePaymentsBetween(order, oldEnd, newEnd);

        addLog(id, ownerId, "续租确认", "续租至" + newEndDate);
        log.info("【续租确认】租约ID:{}, 新到期日:{}", id, newEndDate);

        // 通知租客
        Notification notification = new Notification();
        notification.setUserId(order.getTenantId());
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("续租成功");
        notification.setContent("房东已确认续租，您的租约已续租至 " + newEndDate);
        notificationService.send(notification);
    }

    @Override
    public void quitApply(Integer id, Integer operatorId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("只有进行中的租约才能申请退租");
        }

        order.setStatus(4); // 退租申请中
        order.setQuitApplicant(operatorId);
        rentalOrderMapper.updateById(order);

        addLog(id, operatorId, "退租申请", null);
        log.info("【退租申请】租约ID:{}, 申请人:{}", id, operatorId);

        // 通知对方
        Integer targetUserId = operatorId.equals(order.getTenantId()) ? order.getOwnerId() : order.getTenantId();
        Notification notification = new Notification();
        notification.setUserId(targetUserId);
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("退租申请");
        notification.setContent("租约(ID:" + id + ")收到退租申请，请确认处理。");
        notificationService.send(notification);
    }

    @Override
    public void quitConfirm(Integer id, Integer operatorId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("当前状态无法确认退租");
        }
        if (operatorId.equals(order.getQuitApplicant())) {
            throw new RuntimeException("退租需要对方确认，不能自己确认自己的申请");
        }

        order.setStatus(5); // 已退租
        order.setQuitApplicant(null);
        rentalOrderMapper.updateById(order);

        // 房源恢复上架
        House house = houseMapper.selectById(order.getHouseId());
        if (house != null) {
            house.setStatus(1); // 恢复上架
            house.setUpdateTime(new Date());
            houseMapper.updateById(house);
        }

        addLog(id, operatorId, "退租确认", null);
        log.info("【退租完成】租约ID:{}", id);

        // 通知双方
        Notification nTenant = new Notification();
        nTenant.setUserId(order.getTenantId());
        nTenant.setType(5);
        nTenant.setRelatedId(id);
        nTenant.setTitle("退租完成");
        nTenant.setContent("您的租约(ID:" + id + ")已退租完成。");
        notificationService.send(nTenant);

        Notification nOwner = new Notification();
        nOwner.setUserId(order.getOwnerId());
        nOwner.setType(5);
        nOwner.setRelatedId(id);
        nOwner.setTitle("退租完成");
        nOwner.setContent("租约(ID:" + id + ")已退租完成，房源已恢复上架。");
        notificationService.send(nOwner);
    }

    @Override
    public void quitCancel(Integer id, Integer operatorId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("当前状态无法撤回退租");
        }
        if (!operatorId.equals(order.getQuitApplicant())) {
            throw new RuntimeException("只有退租申请人才能撤回");
        }

        order.setStatus(1); // 恢复进行中
        order.setQuitApplicant(null);
        rentalOrderMapper.updateById(order);

        addLog(id, operatorId, "撤回退租", null);
        log.info("【撤回退租】租约ID:{}, 操作人:{}", id, operatorId);

        // 通知对方
        Integer targetUserId = operatorId.equals(order.getTenantId()) ? order.getOwnerId() : order.getTenantId();
        Notification notification = new Notification();
        notification.setUserId(targetUserId);
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("退租已撤回");
        notification.setContent("租约(ID:" + id + ")的退租申请已被撤回。");
        notificationService.send(notification);
    }

    @Override
    public void cancel(Integer id, Integer operatorId) {
        RentalOrder order = rentalOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("租约不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new RuntimeException("只有待签约状态的订单可以取消");
        }
        if (!operatorId.equals(order.getTenantId()) && !operatorId.equals(order.getOwnerId())) {
            throw new RuntimeException("只有订单当事人才能取消");
        }

        order.setStatus(8); // 已取消
        rentalOrderMapper.updateById(order);

        addLog(id, operatorId, "取消订单", null);
        log.info("【取消订单】租约ID:{}, 操作人:{}", id, operatorId);

        // 通知对方
        Integer targetUserId = operatorId.equals(order.getTenantId()) ? order.getOwnerId() : order.getTenantId();
        Notification notification = new Notification();
        notification.setUserId(targetUserId);
        notification.setType(5);
        notification.setRelatedId(id);
        notification.setTitle("签约订单已取消");
        notification.setContent("租约(ID:" + id + ")已被取消。");
        notificationService.send(notification);
    }

    @Override
    public Page<RentalOrder> getMyList(Integer userId, Integer role, Integer page, Integer size) {
        Page<RentalOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        // 根据角色区分：0租客查tenant_id，1房东查owner_id
        if (role != null && role == 1) {
            wrapper.eq(RentalOrder::getOwnerId, userId);
        } else if (role != null && role == 0) {
            wrapper.eq(RentalOrder::getTenantId, userId);
        } else {
            wrapper.and(w -> w.eq(RentalOrder::getTenantId, userId).or().eq(RentalOrder::getOwnerId, userId));
        }
        wrapper.in(RentalOrder::getStatus, 0, 1, 3, 4, 6) // 待签约、进行中、续租中、退租申请中、待付押金
               .orderByDesc(RentalOrder::getCreateTime);
        Page<RentalOrder> result = rentalOrderMapper.selectPage(pageParam, wrapper);
        fillCurrentMonthRent(result);
        fillHouseInfo(result);
        return result;
    }

    @Override
    public Page<RentalOrder> getHistory(Integer userId, Integer role, Integer page, Integer size) {
        Page<RentalOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        // 根据角色区分
        if (role != null && role == 1) {
            wrapper.eq(RentalOrder::getOwnerId, userId);
        } else if (role != null && role == 0) {
            wrapper.eq(RentalOrder::getTenantId, userId);
        } else {
            wrapper.and(w -> w.eq(RentalOrder::getTenantId, userId).or().eq(RentalOrder::getOwnerId, userId));
        }
        wrapper.in(RentalOrder::getStatus, 2, 5, 7, 8) // 已到期、已退租、已拒绝、已取消
               .orderByDesc(RentalOrder::getCreateTime);
        Page<RentalOrder> result = rentalOrderMapper.selectPage(pageParam, wrapper);
        fillHouseInfo(result);
        return result;
    }

    /**
     * 为订单列表批量填充房源标题和图片，避免 N+1 查询
     */
    private void fillHouseInfo(Page<RentalOrder> page) {
        List<RentalOrder> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Integer> houseIds = records.stream()
                .map(RentalOrder::getHouseId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        if (houseIds.isEmpty()) {
            return;
        }
        List<House> houses = houseMapper.selectBatchIds(houseIds);
        Map<Integer, House> houseMap = new HashMap<>();
        for (House h : houses) {
            houseMap.put(h.getId(), h);
        }
        for (RentalOrder order : records) {
            House h = houseMap.get(order.getHouseId());
            if (h != null) {
                order.setHouseTitle(h.getTitle());
                order.setHouseImages(h.getImages());
            }
        }
    }

    /**
     * 为订单列表填充本月应缴租金
     * 已有缴费记录的从 rent_payment 取；未生成记录的按天折算
     */
    private void fillCurrentMonthRent(Page<RentalOrder> page) {
        String currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        for (RentalOrder order : page.getRecords()) {
            // 先从缴费记录查
            LambdaQueryWrapper<RentPayment> pw = new LambdaQueryWrapper<>();
            pw.eq(RentPayment::getOrderId, order.getId())
              .eq(RentPayment::getPayMonth, currentMonth);
            RentPayment payment = rentPaymentMapper.selectOne(pw);
            if (payment != null) {
                order.setCurrentMonthRent(payment.getAmount());
            } else {
                // 没有缴费记录，按日折算计算预估
                order.setCurrentMonthRent(calcMonthRent(order, currentMonth));
            }
        }
    }

    /**
     * 根据订单起止日期和当前月份，计算该月应缴租金
     * 整月 = monthlyRent，不足一月按 monthlyRent/30 * 天数
     */
    private BigDecimal calcMonthRent(RentalOrder order, String month) {
        if (order.getMonthlyRent() == null || order.getStartDate() == null || order.getEndDate() == null) {
            return order.getMonthlyRent();
        }
        try {
            Calendar monthStart = Calendar.getInstance();
            monthStart.setTime(new SimpleDateFormat("yyyy-MM").parse(month));
            monthStart.set(Calendar.DAY_OF_MONTH, 1);

            Calendar monthEnd = (Calendar) monthStart.clone();
            monthEnd.add(Calendar.MONTH, 1);

            Calendar rentStart = Calendar.getInstance();
            rentStart.setTime(order.getStartDate());
            Calendar rentEnd = Calendar.getInstance();
            rentEnd.setTime(order.getEndDate());

            // 实际计费区间：取交集
            long from = Math.max(monthStart.getTimeInMillis(), rentStart.getTimeInMillis());
            long to = Math.min(monthEnd.getTimeInMillis(), rentEnd.getTimeInMillis());
            if (to <= from) return BigDecimal.ZERO;

            long days = (to - from) / (1000 * 60 * 60 * 24);
            if (days <= 0) return BigDecimal.ZERO;

            // 该月总天数
            int daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (days >= daysInMonth) return order.getMonthlyRent();

            BigDecimal dailyRent = order.getMonthlyRent().divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP);
            return dailyRent.multiply(BigDecimal.valueOf(days));
        } catch (ParseException e) {
            return order.getMonthlyRent();
        }
    }

    /**
     * 签约时自动生成整个租期的月缴费记录
     */
    private void generatePayments(RentalOrder order) {
        generatePaymentsBetween(order, order.getStartDate(), order.getEndDate());
    }

    /**
     * 在指定区间内按月生成缴费记录，最后不满一个月的部分按天折算
     * 日租金 = 月租 / 30，不足一个月按实际天数计费
     * 续租场景下，已存在缴费记录的月份会补足差额，不会重复收费
     */
    private void generatePaymentsBetween(RentalOrder order, Date from, Date to) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM");
        BigDecimal monthlyRent = order.getMonthlyRent();
        // 日租金 = 月租 / 30，保留2位小数
        BigDecimal dailyRent = monthlyRent.divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP);

        while (cal.getTime().before(to)) {
            String payMonth = monthFmt.format(cal.getTime());
            Calendar nextMonth = (Calendar) cal.clone();
            nextMonth.add(Calendar.MONTH, 1);

            // 本月应收金额
            BigDecimal correctAmount;
            if (!nextMonth.getTime().after(to)) {
                correctAmount = monthlyRent;
            } else {
                long remainDays = (to.getTime() - cal.getTime().getTime()) / (1000 * 60 * 60 * 24);
                if (remainDays <= 0) remainDays = 1;
                correctAmount = dailyRent.multiply(BigDecimal.valueOf(remainDays));
            }

            // 检查该月是否已有缴费记录
            LambdaQueryWrapper<RentPayment> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(RentPayment::getOrderId, order.getId())
                        .eq(RentPayment::getPayMonth, payMonth);
            RentPayment existing = rentPaymentMapper.selectOne(existWrapper);

            if (existing == null) {
                // 无记录，直接新建
                RentPayment payment = new RentPayment();
                payment.setOrderId(order.getId());
                payment.setPayMonth(payMonth);
                payment.setAmount(correctAmount);
                payment.setStatus(0);
                payment.setConfirmStatus(0);
                payment.setCreateTime(new Date());
                rentPaymentMapper.insert(payment);
            } else {
                // 已有记录（续租补差场景）：补足续租区间的费用
                // 总额不超过整月租金（上限封顶）
                BigDecimal newTotal = existing.getAmount().add(correctAmount);
                if (newTotal.compareTo(monthlyRent) > 0) {
                    newTotal = monthlyRent;
                }
                BigDecimal supplement = newTotal.subtract(existing.getAmount());
                if (supplement.compareTo(BigDecimal.ZERO) > 0) {
                    if (existing.getStatus() == 0) {
                        // 未支付：直接更新金额
                        existing.setAmount(newTotal);
                        rentPaymentMapper.updateById(existing);
                    } else {
                        // 已支付：新建一条补差记录
                        RentPayment supplementPayment = new RentPayment();
                        supplementPayment.setOrderId(order.getId());
                        supplementPayment.setPayMonth(payMonth);
                        supplementPayment.setAmount(supplement);
                        supplementPayment.setStatus(0);
                        supplementPayment.setConfirmStatus(0);
                        supplementPayment.setCreateTime(new Date());
                        rentPaymentMapper.insert(supplementPayment);
                    }
                }
            }

            cal.add(Calendar.MONTH, 1);
        }
    }

    private void addLog(Integer orderId, Integer operatorId, String action, String remark) {
        RentalOrderLog orderLog = new RentalOrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setOperatorId(operatorId);
        orderLog.setAction(action);
        orderLog.setRemark(remark);
        orderLog.setCreateTime(new Date());
        rentalOrderLogMapper.insert(orderLog);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private Date truncateToDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
