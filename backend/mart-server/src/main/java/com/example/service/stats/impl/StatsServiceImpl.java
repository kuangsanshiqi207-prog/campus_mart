package com.example.service.stats.impl;

import com.example.constant.ProductConstant;
import com.example.context.BaseContext;
import com.example.entity.CreditLog;
import com.example.entity.User;
import com.example.mapper.message.MessageMapper;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.stats.CreditLogMapper;
import com.example.mapper.stats.StatsMapper;
import com.example.mapper.user.UserMapper;
import com.example.result.PageResult;
import com.example.service.stats.StatsService;
import com.example.vo.product.UserProductStatsVO;
import com.example.vo.stats.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;
    private final CreditLogMapper creditLogMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    // ==================== 概览 ====================

    @Override
    public UserOverviewVO getOverview() {
        Long userId = BaseContext.getCurrentId();

        User user = userMapper.getById(userId);
        Integer creditScore = user != null ? user.getCreditScore() : 0;

        Integer productTotal = statsMapper.countMyProducts(userId, null);
        Integer productOnSale = statsMapper.countMyProducts(userId, ProductConstant.STATUS_ON_SALE);
        Integer productSold = statsMapper.countMyProducts(userId, ProductConstant.STATUS_SOLD);

        Integer orderBuyTotal = statsMapper.countMyOrders(userId, "buy", null);
        Integer orderSellTotal = statsMapper.countMyOrders(userId, "sell", null);
        Integer orderPending = statsMapper.countMyOrders(userId, "buy", "pending")
                + statsMapper.countMyOrders(userId, "sell", "pending");

        Integer reviewCount = statsMapper.countReceivedReviews(userId);
        UserReviewStatsVO reviewStats = statsMapper.getMyReviewStats(userId);
        Double avgScore = reviewStats != null ? reviewStats.getAvgScore() : 0.0;

        Integer favoriteCount = statsMapper.countMyFavorites(userId);
        Long unreadMessage = messageMapper.countUnread(userId);

        return UserOverviewVO.builder()
                .productTotal(productTotal)
                .productOnSale(productOnSale)
                .productSold(productSold)
                .orderBuyTotal(orderBuyTotal)
                .orderSellTotal(orderSellTotal)
                .orderPending(orderPending)
                .creditScore(creditScore)
                .reviewCount(reviewCount)
                .avgScore(avgScore)
                .favoriteCount(favoriteCount)
                .unreadMessage(unreadMessage)
                .build();
    }

    // ==================== 我的商品统计 ====================

    @Override
    public UserProductStatsVO getMyProductStats() {
        Long userId = BaseContext.getCurrentId();
        UserProductStatsVO stats = productMapper.getMyStats(userId);
        if (stats == null) {
            stats = UserProductStatsVO.builder()
                    .total(0).onSale(0).reserved(0).sold(0)
                    .offline(0).pending(0).rejected(0)
                    .viewTotal(0L).favoriteTotal(0L)
                    .build();
        }
        return stats;
    }

    // ==================== 我的交易统计 ====================

    @Override
    public UserOrderStatsVO getMyOrderStats() {
        Long userId = BaseContext.getCurrentId();
        UserOrderStatsVO stats = statsMapper.getMyOrderStats(userId);
        if (stats == null) {
            stats = UserOrderStatsVO.builder()
                    .buyTotal(0).sellTotal(0)
                    .buyCompleted(0).sellCompleted(0)
                    .buyAmount(java.math.BigDecimal.ZERO)
                    .sellAmount(java.math.BigDecimal.ZERO)
                    .pending(0).refunding(0).dispute(0)
                    .build();
        }
        return stats;
    }

    // ==================== 我的评价统计 ====================

    @Override
    public UserReviewStatsVO getMyReviewStats() {
        Long userId = BaseContext.getCurrentId();
        UserReviewStatsVO stats = statsMapper.getMyReviewStats(userId);
        if (stats == null) {
            stats = UserReviewStatsVO.builder()
                    .receivedTotal(0).sentTotal(0).avgScore(0.0)
                    .score5Count(0).score4Count(0).score3Count(0)
                    .score2Count(0).score1Count(0).goodRate(0.0)
                    .build();
        }
        // 发出的评价数
        Integer sentTotal = countSentReviews(userId);
        stats.setSentTotal(sentTotal);
        return stats;
    }

    private Integer countSentReviews(Long userId) {
        // 简单实现：用 statsMapper 的 countMyOrders 类似的方法，实际用 SQL 查
        // 这里用 StatsMapper 里加一个方法更合适，MVP 阶段用 0 占位
        return 0;
    }

    // ==================== 信用分流水 ====================

    @Override
    public PageResult listCreditLogs(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = creditLogMapper.countByUserId(userId);
        List<CreditLog> list = creditLogMapper.listByUserId(userId, offset, pageSize);

        List<CreditLogVO> voList = list.stream()
                .map(this::toCreditLogVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 信用分趋势 ====================

    @Override
    public TrendVO getCreditTrend(Integer days) {
        Long userId = BaseContext.getCurrentId();
        if (days == null || days < 1 || days > 365) days = 30;

        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<Map<String, Object>> rows = statsMapper.getCreditTrend(userId, startDateStr);

        return buildTrend(rows, startDate, days);
    }

    // ==================== 交易趋势 ====================

    @Override
    public TrendVO getOrderTrend(String type, Integer days) {
        Long userId = BaseContext.getCurrentId();
        if (type == null || type.isEmpty()) type = "buy";
        if (days == null || days < 1 || days > 365) days = 30;

        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<Map<String, Object>> rows = statsMapper.getOrderTrend(userId, type, startDateStr);

        return buildTrend(rows, startDate, days);
    }

    // ==================== 私有方法 ====================

    private TrendVO buildTrend(List<Map<String, Object>> rows, LocalDate startDate, Integer days) {
        // 用 Map 快速查找
        Map<String, Long> dataMap = rows.stream()
                .collect(Collectors.toMap(
                        r -> String.valueOf(r.get("date")),
                        r -> ((Number) r.get("value")).longValue()
                ));

        List<LocalDate> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            dates.add(date);
            String key = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            values.add(dataMap.getOrDefault(key, 0L));
        }

        return TrendVO.builder()
                .dates(dates)
                .values(values)
                .build();
    }

    private Integer normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) return 1;
        return pageNum;
    }

    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1 || pageSize > 100) return 10;
        return pageSize;
    }

    private CreditLogVO toCreditLogVO(CreditLog log) {
        CreditLogVO vo = new CreditLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }
}