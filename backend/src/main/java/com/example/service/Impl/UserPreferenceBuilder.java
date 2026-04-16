package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.Appointment;
import com.example.entity.House;
import com.example.entity.HouseFavorite;
import com.example.entity.HouseViewLog;
import com.example.entity.UserPreference;
import com.example.mapper.AppointmentMapper;
import com.example.mapper.HouseFavoriteMapper;
import com.example.mapper.HouseMapper;
import com.example.mapper.HouseViewLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于隐式反馈的用户偏好画像构建器
 *
 * 核心算法：行为加权 + 指数时间衰减 + 加权特征聚合
 *   1) 行为加权：浏览=1、收藏=5、预约=8，量化不同行为的兴趣强度
 *   2) 时间衰减：w = w0 * exp(-λ·Δt)，λ=ln2/30 即半衰期 30 天，捕捉兴趣漂移
 *   3) 加权聚合：类别特征取加权频次 Top-1；价格取加权中位数 ±30% 生成偏好区间
 *
 * 输出结构与 user_preference 表对齐，可直接喂给 RecommendServiceImpl 的逐级降级召回。
 */
@Component
@Slf4j
public class UserPreferenceBuilder {

    private static final double VIEW_WEIGHT = 1.0;
    private static final double FAVORITE_WEIGHT = 5.0;
    private static final double APPOINTMENT_WEIGHT = 8.0;

    /** λ = ln(2) / 30，对应半衰期 30 天 */
    private static final double DECAY_LAMBDA = Math.log(2) / 30.0;

    /** 只采样最近 N 天的行为，更早的行为衰减后贡献趋近 0，直接剪枝 */
    private static final int LOOKBACK_DAYS = 90;

    /** 价格偏好区间：加权中位数的 [0.7, 1.3] 倍 */
    private static final double PRICE_LOWER_RATIO = 0.7;
    private static final double PRICE_UPPER_RATIO = 1.3;

    private static final long MS_PER_DAY = 24L * 3600L * 1000L;

    @Autowired
    private HouseViewLogMapper houseViewLogMapper;
    @Autowired
    private HouseFavoriteMapper houseFavoriteMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private HouseMapper houseMapper;

    /**
     * 根据用户最近的隐式行为动态构建偏好画像。
     * 若用户在观察窗口内没有任何行为，返回 null（由上层走冷启动兜底）。
     */
    public UserPreference build(Integer userId) {
        if (userId == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        Date cutoffDate = new Date(now - LOOKBACK_DAYS * MS_PER_DAY);

        // 1. 收集三类隐式行为
        List<WeightedBehavior> behaviors = new ArrayList<>();
        collectViews(userId, cutoffDate, behaviors);
        collectFavorites(userId, cutoffDate, behaviors);
        collectAppointments(userId, cutoffDate, behaviors);

        if (behaviors.isEmpty()) {
            return null;
        }

        // 2. 指数时间衰减
        for (WeightedBehavior b : behaviors) {
            double deltaDays = Math.max(0, (now - b.timestamp) / (double) MS_PER_DAY);
            b.weight *= Math.exp(-DECAY_LAMBDA * deltaDays);
        }

        // 3. 批量查询房源特征
        Set<Integer> houseIds = new HashSet<>();
        for (WeightedBehavior b : behaviors) {
            houseIds.add(b.houseId);
        }
        Map<Integer, House> houseMap = new HashMap<>();
        if (!houseIds.isEmpty()) {
            List<House> houses = houseMapper.selectBatchIds(houseIds);
            for (House h : houses) {
                houseMap.put(h.getId(), h);
            }
        }

        // 4. 加权特征聚合
        Map<String, Double> provinceScores = new HashMap<>();
        Map<String, Double> cityScores = new HashMap<>();
        Map<String, Double> districtScores = new HashMap<>();
        Map<Integer, Double> houseTypeScores = new HashMap<>();
        Map<Integer, Double> roomCountScores = new HashMap<>();
        List<double[]> priceWeights = new ArrayList<>();

        for (WeightedBehavior b : behaviors) {
            House h = houseMap.get(b.houseId);
            if (h == null) {
                continue;
            }
            double w = b.weight;
            addScore(provinceScores, h.getProvince(), w);
            addScore(cityScores, h.getCity(), w);
            addScore(districtScores, h.getDistrict(), w);
            if (h.getHouseType() != null) {
                houseTypeScores.merge(h.getHouseType(), w, Double::sum);
            }
            if (h.getRoomCount() != null) {
                roomCountScores.merge(h.getRoomCount(), w, Double::sum);
            }
            if (h.getPrice() != null) {
                priceWeights.add(new double[]{h.getPrice().doubleValue(), w});
            }
        }

        // 5. 装配偏好对象
        UserPreference pref = new UserPreference();
        pref.setUserId(userId);
        pref.setPreferredProvince(argMax(provinceScores));
        pref.setPreferredCity(argMax(cityScores));
        pref.setPreferredDistrict(argMax(districtScores));
        pref.setPreferredHouseType(argMax(houseTypeScores));
        pref.setPreferredRoomCount(argMax(roomCountScores));

        Double median = weightedMedian(priceWeights);
        if (median != null && median > 0) {
            pref.setPriceMin(BigDecimal.valueOf(median * PRICE_LOWER_RATIO)
                    .setScale(2, RoundingMode.HALF_UP));
            pref.setPriceMax(BigDecimal.valueOf(median * PRICE_UPPER_RATIO)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        log.info("[UserPreferenceBuilder] userId={} city={} district={} type={} rooms={} priceRange=[{},{}] behaviors={}",
                userId, pref.getPreferredCity(), pref.getPreferredDistrict(),
                pref.getPreferredHouseType(), pref.getPreferredRoomCount(),
                pref.getPriceMin(), pref.getPriceMax(), behaviors.size());

        return pref;
    }

    private void collectViews(Integer userId, Date cutoff, List<WeightedBehavior> out) {
        LambdaQueryWrapper<HouseViewLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseViewLog::getUserId, userId)
                .ge(HouseViewLog::getCreateTime, cutoff);
        for (HouseViewLog log : houseViewLogMapper.selectList(wrapper)) {
            if (log.getHouseId() != null && log.getCreateTime() != null) {
                out.add(new WeightedBehavior(log.getHouseId(),
                        log.getCreateTime().getTime(), VIEW_WEIGHT));
            }
        }
    }

    private void collectFavorites(Integer userId, Date cutoff, List<WeightedBehavior> out) {
        LambdaQueryWrapper<HouseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseFavorite::getUserId, userId)
                .ge(HouseFavorite::getCreateTime, cutoff);
        for (HouseFavorite fav : houseFavoriteMapper.selectList(wrapper)) {
            if (fav.getHouseId() != null && fav.getCreateTime() != null) {
                out.add(new WeightedBehavior(fav.getHouseId(),
                        fav.getCreateTime().getTime(), FAVORITE_WEIGHT));
            }
        }
    }

    private void collectAppointments(Integer userId, Date cutoff, List<WeightedBehavior> out) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getUserId, userId)
                .ge(Appointment::getCreateTime, cutoff);
        for (Appointment apt : appointmentMapper.selectList(wrapper)) {
            if (apt.getHouseId() != null && apt.getCreateTime() != null) {
                out.add(new WeightedBehavior(apt.getHouseId(),
                        apt.getCreateTime().getTime(), APPOINTMENT_WEIGHT));
            }
        }
    }

    private static void addScore(Map<String, Double> scores, String key, double w) {
        if (key != null && !key.isEmpty()) {
            scores.merge(key, w, Double::sum);
        }
    }

    /** 取加权分数最高的 key，空 map 返回 null */
    private static <K> K argMax(Map<K, Double> scores) {
        K best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<K, Double> e : scores.entrySet()) {
            if (e.getValue() > bestScore) {
                bestScore = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    /**
     * 加权中位数：按值升序累加权重，取首个累加值 ≥ 总权重/2 的元素。
     * 相比加权均值，对离群值（如误点的豪宅）具有鲁棒性。
     */
    private static Double weightedMedian(List<double[]> samples) {
        if (samples == null || samples.isEmpty()) {
            return null;
        }
        samples.sort(Comparator.comparingDouble(a -> a[0]));
        double total = 0;
        for (double[] s : samples) {
            total += s[1];
        }
        if (total <= 0) {
            return null;
        }
        double half = total / 2.0;
        double cumulative = 0;
        for (double[] s : samples) {
            cumulative += s[1];
            if (cumulative >= half) {
                return s[0];
            }
        }
        return samples.get(samples.size() - 1)[0];
    }

    private static class WeightedBehavior {
        final Integer houseId;
        final long timestamp;
        double weight;

        WeightedBehavior(Integer houseId, long timestamp, double weight) {
            this.houseId = houseId;
            this.timestamp = timestamp;
            this.weight = weight;
        }
    }
}
