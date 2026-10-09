package com.example.service.stats.impl;

import com.example.api.message.MessageFeignClient;
import com.example.api.user.UserFeignClient;
import com.example.constant.ProductConstant;
import com.example.context.BaseContext;
import com.example.mapper.product.ProductMapper;
import com.example.mapper.stats.StatsMapper;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.stats.StatsService;
import com.example.vo.product.UserProductStatsVO;
import com.example.vo.stats.*;
import com.example.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProductMapper productMapper;
    private final UserFeignClient userFeignClient;
    private final MessageFeignClient messageFeignClient;

    // ==================== 概览 ====================

    @Override
    public UserOverviewVO getOverview() {
        Long userId = BaseContext.getCurrentId();

        Integer creditScore = 0;
        Result<UserVO> ur = userFeignClient.getById(userId);
        if (ur != null && ur.getCode() == 1 && ur.getData() != null) {
            creditScore = ur.getData().getCreditScore();
        }

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

        Long unreadMessage = 0L;
        Result<Long> mr = messageFeignClient.countUnread(userId);
        if (mr != null && mr.getCode() == 1 && mr.getData() != null) {
            unreadMessage = mr.getData();
        }

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
        stats.setSentTotal(0);
        return stats;
    }

    // ==================== 信用分流水（通过 Feign 调 user-service）====================

    @Override
    public PageResult listCreditLogs(Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1 || pageSize > 100) pageSize = 10;

        Result<PageResult> r = userFeignClient.listCreditLogs(userId, pageNum, pageSize);
        if (r != null && r.getCode() == 1 && r.getData() != null) {
            return r.getData();
        }
        return new PageResult(0L, List.of());
    }

    // ==================== 信用分趋势（继续用 statsMapper）====================

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
}