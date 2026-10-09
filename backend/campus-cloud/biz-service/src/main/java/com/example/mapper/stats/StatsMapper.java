package com.example.mapper.stats;

import com.example.vo.stats.UserOrderStatsVO;
import com.example.vo.stats.UserReviewStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {

    /** 我的商品总数 */
    Integer countMyProducts(@Param("userId") Long userId,
                            @Param("status") String status);

    /** 我的订单数 */
    Integer countMyOrders(@Param("userId") Long userId,
                          @Param("role") String role,
                          @Param("status") String status);

    /** 我的订单统计 */
    UserOrderStatsVO getMyOrderStats(@Param("userId") Long userId);

    /** 我的评价统计 */
    UserReviewStatsVO getMyReviewStats(@Param("userId") Long userId);

    /** 我收到的评价数 */
    Integer countReceivedReviews(Long userId);

    /** 我的收藏数 */
    Integer countMyFavorites(Long userId);

    /** 信用分趋势：按天查询 */
    List<Map<String, Object>> getCreditTrend(@Param("userId") Long userId,
                                             @Param("startDate") String startDate);

    /** 订单趋势：按天查询 */
    List<Map<String, Object>> getOrderTrend(@Param("userId") Long userId,
                                            @Param("role") String role,
                                            @Param("startDate") String startDate);
}