package com.example.service.stats;

import com.example.result.PageResult;
import com.example.vo.product.UserProductStatsVO;
import com.example.vo.stats.*;

public interface StatsService {

    UserOverviewVO getOverview();

    UserProductStatsVO getMyProductStats();

    UserOrderStatsVO getMyOrderStats();

    UserReviewStatsVO getMyReviewStats();

    PageResult listCreditLogs(Integer pageNum, Integer pageSize);

    TrendVO getCreditTrend(Integer days);

    TrendVO getOrderTrend(String type, Integer days);
}