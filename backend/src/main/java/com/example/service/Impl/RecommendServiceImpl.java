package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.SearchResult;
import com.example.entity.House;
import com.example.entity.UserPreference;
import com.example.mapper.HouseMapper;
import com.example.mapper.UserPreferenceMapper;
import com.example.service.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 房源推荐服务实现
 *
 * 核心推荐策略：偏好匹配 + 区域逐级降级 + 热门兜底
 *
 * 推荐算法分为三个场景：
 * 1. recommendForUser   —— 首页个性化推荐，纯推荐列表（不分页）
 * 2. recommendWithFilter —— 找房页面，推荐排序 + 用户筛选条件 + 分页
 * 3. getSimilar          —— 房源详情页，基于当前房源属性的相似推荐
 */
@Service
@Slf4j
public class RecommendServiceImpl implements RecommendService {
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private UserPreferenceMapper userPreferenceMapper;
    @Autowired
    private UserPreferenceBuilder userPreferenceBuilder;

    /**
     * 首页个性化推荐（不分页）
     *
     * 逐级降级策略：
     *   第1轮：精确匹配（省+市+区+价格+类型+户型）
     *   第2轮：放宽区 → 同城匹配
     *   第3轮：放宽市 → 同省匹配
     *   第4轮：放宽价格和类型 → 同城热门
     *   第5轮：全站热门兜底（按收藏量+浏览量排序）
     *
     * 每轮查询都排除前面已选中的房源ID，避免重复。
     * 新用户（无偏好数据）直接跳到第5轮全站热门。
     *
     * @param userId 用户ID
     * @param limit  最大返回数量
     * @return 推荐房源列表
     */
    @Override
    public List<House> recommendForUser(Integer userId, Integer limit) {
        // 1. 获取用户偏好：优先显式偏好表，缺失时回落到隐式行为建模
        UserPreference preference = null;
        if (userId != null) {
            LambdaQueryWrapper<UserPreference> prefWrapper = new LambdaQueryWrapper<>();
            prefWrapper.eq(UserPreference::getUserId, userId);
            preference = userPreferenceMapper.selectOne(prefWrapper);
            if (!hasUsefulPreference(preference)) {
                // 显式偏好为空或全字段为空：基于浏览/收藏/预约日志动态建模
                preference = userPreferenceBuilder.build(userId);
            }
        }

        List<House> result = new ArrayList<>();
        Set<Integer> existingIds = new HashSet<>(); // 已选房源ID集合，用于去重

        if (preference != null) {
            // 第1轮：精确匹配偏好（省+市+区+价格+类型+户型）
            result.addAll(queryHouses(preference.getPreferredProvince(), preference.getPreferredCity(),
                    preference.getPreferredDistrict(), preference, limit, existingIds));

            // 第2轮：放宽区 → 只匹配同城（省+市+价格+类型+户型）
            if (result.size() < limit && StringUtils.isNotBlank(preference.getPreferredDistrict())) {
                result.addAll(queryHouses(preference.getPreferredProvince(), preference.getPreferredCity(),
                        null, preference, limit - result.size(), existingIds));
            }

            // 第3轮：放宽市 → 只匹配同省（省+价格+类型+户型）
            if (result.size() < limit && StringUtils.isNotBlank(preference.getPreferredCity())) {
                result.addAll(queryHouses(preference.getPreferredProvince(), null,
                        null, preference, limit - result.size(), existingIds));
            }

            // 第4轮：放宽价格和类型 → 同城热门（仅按区域，不限价格和类型）
            if (result.size() < limit && StringUtils.isNotBlank(preference.getPreferredCity())) {
                result.addAll(queryHouses(preference.getPreferredProvince(), preference.getPreferredCity(),
                        null, null, limit - result.size(), existingIds));
            }
        }

        // 第5轮（兜底）：全站热门，按收藏量和浏览量倒序
        if (result.size() < limit) {
            LambdaQueryWrapper<House> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.eq(House::getStatus, 1); // 只查已上架房源
            if (!existingIds.isEmpty()) {
                fallbackWrapper.notIn(House::getId, existingIds);
            }
            fallbackWrapper.orderByDesc(House::getFavoriteCount)
                           .orderByDesc(House::getViewCount)
                           .last("LIMIT " + (limit - result.size()));
            result.addAll(houseMapper.selectList(fallbackWrapper));
        }

        return result;
    }

    /**
     * 判断显式偏好是否有效：对象非空且至少有一个字段可用于匹配。
     * 否则回落到隐式行为建模。
     */
    private boolean hasUsefulPreference(UserPreference p) {
        if (p == null) {
            return false;
        }
        return StringUtils.isNotBlank(p.getPreferredProvince())
                || StringUtils.isNotBlank(p.getPreferredCity())
                || StringUtils.isNotBlank(p.getPreferredDistrict())
                || p.getPreferredHouseType() != null
                || p.getPreferredRoomCount() != null
                || p.getPriceMin() != null
                || p.getPriceMax() != null;
    }

    /**
     * 按区域和偏好条件查询房源
     *
     * @param province   省份筛选（可为null表示不限）
     * @param city       城市筛选（可为null表示不限）
     * @param district   区县筛选（可为null表示不限）
     * @param pref       用户偏好（包含价格、类型、户型条件；null表示不限偏好）
     * @param limit      最大返回数量
     * @param excludeIds 需排除的房源ID集合（已在前面轮次选中的），查询结果会自动追加到此集合
     * @return 匹配的房源列表
     */
    private List<House> queryHouses(String province, String city, String district,
                                     UserPreference pref, int limit, Set<Integer> excludeIds) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, 1); // 只查已上架房源

        // 区域条件（逐级可选）
        if (StringUtils.isNotBlank(province)) {
            wrapper.eq(House::getProvince, province);
        }
        if (StringUtils.isNotBlank(city)) {
            wrapper.eq(House::getCity, city);
        }
        if (StringUtils.isNotBlank(district)) {
            wrapper.eq(House::getDistrict, district);
        }
        // 偏好条件（价格区间、房屋类型、户型）
        if (pref != null) {
            if (pref.getPriceMin() != null) wrapper.ge(House::getPrice, pref.getPriceMin());
            if (pref.getPriceMax() != null) wrapper.le(House::getPrice, pref.getPriceMax());
            if (pref.getPreferredHouseType() != null) wrapper.eq(House::getHouseType, pref.getPreferredHouseType());
            if (pref.getPreferredRoomCount() != null) wrapper.eq(House::getRoomCount, pref.getPreferredRoomCount());
        }
        // 排除已选房源
        if (!excludeIds.isEmpty()) {
            wrapper.notIn(House::getId, excludeIds);
        }

        // 按热度排序（收藏数 > 浏览数）
        wrapper.orderByDesc(House::getFavoriteCount)
               .orderByDesc(House::getViewCount)
               .last("LIMIT " + limit);

        List<House> houses = houseMapper.selectList(wrapper);
        // 将本轮结果加入排除集合
        for (House h : houses) {
            excludeIds.add(h.getId());
        }
        return houses;
    }

    /**
     * 找房页面：推荐排序 + 筛选条件 + 分页
     *
     * 工作流程：
     * 1. 先获取用户的个性化推荐列表（用于排序权重）
     * 2. 按筛选条件查询所有匹配的已上架房源
     * 3. 如果精确匹配无结果，逐步放宽条件（价格±50% → 去类型 → 去区限制 → 去关键词 → 同城热门 → 全站兜底）
     * 4. 对结果排序：推荐列表中的房源排前面（按推荐顺序），其余按热度分排序
     * 5. 手动分页返回
     *
     * 热度分公式：收藏数 × 3 + 浏览数
     */
    @Override
    public SearchResult recommendWithFilter(Integer userId, String keyword, String province, String city,
                                            String district, Integer houseType, Integer minPrice, Integer maxPrice,
                                            Integer page, Integer size) {
        // 获取推荐列表（多取一些用于排序权重计算）
        List<House> recommended = recommendForUser(userId, size * page + 50);
        // 用 LinkedHashSet 保持推荐顺序，后续排序时按插入顺序判断优先级
        Set<Integer> recIds = new LinkedHashSet<>();
        for (House h : recommended) {
            recIds.add(h.getId());
        }

        // 按用户筛选条件精确查询
        List<House> allMatched = doFilterQuery(keyword, province, city, district, houseType, minPrice, maxPrice);

        // 精确匹配有结果，直接按推荐排序后分页返回
        if (!allMatched.isEmpty()) {
            return new SearchResult(buildSortedPage(allMatched, recIds, page, size), null);
        }

        // ---- 以下为无精确结果时的逐步放宽逻辑 ----

        boolean hasFilter = StringUtils.isNotBlank(keyword) || StringUtils.isNotBlank(province)
                || StringUtils.isNotBlank(city) || StringUtils.isNotBlank(district)
                || houseType != null || minPrice != null || maxPrice != null;
        if (!hasFilter) {
            // 没有任何筛选条件，确实没有可用房源
            return new SearchResult(buildSortedPage(allMatched, recIds, page, size), null);
        }

        List<String> relaxed = new ArrayList<>();

        // 放宽第1步：扩大价格范围（原价格的±50%）
        if (minPrice != null || maxPrice != null) {
            Integer relaxMin = minPrice != null ? (int)(minPrice * 0.5) : null;
            Integer relaxMax = maxPrice != null ? (int)(maxPrice * 1.5) : null;
            allMatched = doFilterQuery(keyword, province, city, district, houseType, relaxMin, relaxMax);
            if (!allMatched.isEmpty()) {
                relaxed.add("扩大了价格范围");
                return new SearchResult(buildSortedPage(allMatched, recIds, page, size),
                        "未找到完全匹配的房源，已为您" + String.join("、", relaxed) + "，推荐以下相近房源");
            }
        }

        // 放宽第2步：去掉房屋类型限制（整租/合租不限）
        if (houseType != null) {
            Integer relaxMin = minPrice != null ? (int)(minPrice * 0.5) : null;
            Integer relaxMax = maxPrice != null ? (int)(maxPrice * 1.5) : null;
            allMatched = doFilterQuery(keyword, province, city, district, null, relaxMin, relaxMax);
            if (!allMatched.isEmpty()) {
                relaxed.add("扩大了价格范围");
                relaxed.add("不限房屋类型");
                return new SearchResult(buildSortedPage(allMatched, recIds, page, size),
                        "未找到完全匹配的房源，已为您" + String.join("、", relaxed) + "，推荐以下相近房源");
            }
        }

        // 放宽第3步：去掉区限制（保留城市级别）
        if (StringUtils.isNotBlank(district)) {
            allMatched = doFilterQuery(keyword, province, city, null, null, null, null);
            if (!allMatched.isEmpty()) {
                relaxed.clear();
                relaxed.add("扩大了搜索范围至全城");
                return new SearchResult(buildSortedPage(allMatched, recIds, page, size),
                        "未找到完全匹配的房源，已为您" + String.join("、", relaxed) + "，推荐以下相近房源");
            }
        }

        // 放宽第4步：去掉所有地域限制，仅保留关键词
        if (StringUtils.isNotBlank(keyword)) {
            allMatched = doFilterQuery(keyword, null, null, null, null, null, null);
            if (!allMatched.isEmpty()) {
                return new SearchResult(buildSortedPage(allMatched, recIds, page, size),
                        "未找到完全匹配的房源，已为您扩大搜索范围，推荐以下相近房源");
            }
        }

        // 放宽第5步：同城热门兜底
        if (StringUtils.isNotBlank(city)) {
            allMatched = doFilterQuery(null, province, city, null, null, null, null);
            if (!allMatched.isEmpty()) {
                return new SearchResult(buildSortedPage(allMatched, recIds, page, size),
                        "未找到符合条件的房源，为您推荐" + city + "的热门房源");
            }
        }

        // 最终兜底：全站热门
        allMatched = doFilterQuery(null, null, null, null, null, null, null);
        String hint = allMatched.isEmpty() ? null : "未找到符合条件的房源，为您推荐热门房源";
        return new SearchResult(buildSortedPage(allMatched, recIds, page, size), hint);
    }

    /**
     * 按筛选条件查询已上架房源（不分页，返回全部匹配结果）
     * 用于 recommendWithFilter 中的精确查询和放宽查询
     */
    private List<House> doFilterQuery(String keyword, String province, String city,
                                       String district, Integer houseType, Integer minPrice, Integer maxPrice) {
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, 1);
        if (StringUtils.isNotBlank(keyword)) {
            // 关键词同时搜索标题和描述
            wrapper.and(w -> w.like(House::getTitle, keyword).or().like(House::getDescription, keyword));
        }
        if (StringUtils.isNotBlank(province)) wrapper.eq(House::getProvince, province);
        if (StringUtils.isNotBlank(city)) wrapper.eq(House::getCity, city);
        if (StringUtils.isNotBlank(district)) wrapper.eq(House::getDistrict, district);
        if (houseType != null) wrapper.eq(House::getHouseType, houseType);
        if (minPrice != null) wrapper.ge(House::getPrice, minPrice);
        if (maxPrice != null) wrapper.le(House::getPrice, maxPrice);
        return houseMapper.selectList(wrapper);
    }

    /**
     * 对查询结果按推荐优先级排序，然后手动分页
     *
     * 排序规则：
     * - 在推荐列表中的房源排前面，按推荐列表中的原始顺序
     * - 不在推荐列表中的房源排后面，按热度分（收藏数×3 + 浏览数）降序
     *
     * @param allMatched 所有匹配的房源（未排序）
     * @param recIds     推荐列表中的房源ID（LinkedHashSet，保持推荐顺序）
     * @param page       当前页码（从1开始）
     * @param size       每页数量
     * @return 分页结果
     */
    private Page<House> buildSortedPage(List<House> allMatched, Set<Integer> recIds, int page, int size) {
        allMatched.sort((a, b) -> {
            boolean aRec = recIds.contains(a.getId());
            boolean bRec = recIds.contains(b.getId());
            // 两个都在推荐列表中：按推荐顺序（在 recIds 中的位置）
            if (aRec && bRec) {
                return indexOf(recIds, a.getId()) - indexOf(recIds, b.getId());
            }
            // 只有 a 在推荐列表中：a 排前面
            if (aRec) return -1;
            // 只有 b 在推荐列表中：b 排前面
            if (bRec) return 1;
            // 都不在推荐列表中：按热度分降序（收藏数×3 + 浏览数）
            int scoreA = (a.getFavoriteCount() != null ? a.getFavoriteCount() : 0) * 3
                       + (a.getViewCount() != null ? a.getViewCount() : 0);
            int scoreB = (b.getFavoriteCount() != null ? b.getFavoriteCount() : 0) * 3
                       + (b.getViewCount() != null ? b.getViewCount() : 0);
            return scoreB - scoreA;
        });

        // 手动分页：从排序后的完整列表中截取当前页
        int total = allMatched.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<House> pageList = from < total ? allMatched.subList(from, to) : Collections.emptyList();

        Page<House> result = new Page<>(page, size, total);
        result.setRecords(pageList);
        return result;
    }

    /**
     * 获取元素在 LinkedHashSet 中的位置索引
     * 用于保持推荐列表的原始顺序
     */
    private int indexOf(Set<Integer> set, Integer id) {
        int i = 0;
        for (Integer item : set) {
            if (item.equals(id)) return i;
            i++;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 相似房源推荐（房源详情页底部）
     *
     * 基于当前房源的属性查找相似房源，逐级降级：
     *   第1轮：同城+同区+同类型+价格±30%
     *   第2轮：放宽到同城（去掉区和价格限制）
     *   第3轮：全站热门兜底
     *
     * 结果按浏览量倒序排列
     *
     * @param houseId 当前房源ID
     * @param limit   最大返回数量
     * @return 相似房源列表
     */
    @Override
    public List<House> getSimilar(Integer houseId, Integer limit) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            return Collections.emptyList();
        }

        // 第1轮：同城+同区+同类型+价格±30%（最精确的相似匹配）
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, 1)
               .ne(House::getId, houseId); // 排除当前房源

        if (StringUtils.isNotBlank(house.getCity())) {
            wrapper.eq(House::getCity, house.getCity());
        }
        if (StringUtils.isNotBlank(house.getDistrict())) {
            wrapper.eq(House::getDistrict, house.getDistrict());
        }
        if (house.getHouseType() != null) {
            wrapper.eq(House::getHouseType, house.getHouseType());
        }
        // 价格浮动30%范围内
        if (house.getPrice() != null) {
            wrapper.ge(House::getPrice, house.getPrice().multiply(new java.math.BigDecimal("0.7")))
                   .le(House::getPrice, house.getPrice().multiply(new java.math.BigDecimal("1.3")));
        }

        wrapper.orderByDesc(House::getViewCount)
               .last("LIMIT " + limit);

        List<House> result = houseMapper.selectList(wrapper);

        // 第2轮：放宽到同城（去掉区和价格限制）
        if (result.size() < limit && StringUtils.isNotBlank(house.getCity())) {
            Set<Integer> existingIds = result.stream().map(House::getId).collect(Collectors.toSet());
            existingIds.add(houseId);
            LambdaQueryWrapper<House> cityWrapper = new LambdaQueryWrapper<>();
            cityWrapper.eq(House::getStatus, 1)
                       .eq(House::getCity, house.getCity())
                       .notIn(House::getId, existingIds)
                       .orderByDesc(House::getViewCount)
                       .last("LIMIT " + (limit - result.size()));
            result.addAll(houseMapper.selectList(cityWrapper));
        }

        // 第3轮（兜底）：全站热门
        if (result.size() < limit) {
            Set<Integer> existingIds = result.stream().map(House::getId).collect(Collectors.toSet());
            existingIds.add(houseId);
            LambdaQueryWrapper<House> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.eq(House::getStatus, 1)
                           .notIn(House::getId, existingIds)
                           .orderByDesc(House::getViewCount)
                           .last("LIMIT " + (limit - result.size()));
            result.addAll(houseMapper.selectList(fallbackWrapper));
        }

        return result;
    }
}
