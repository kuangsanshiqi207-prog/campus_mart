package com.example.mapper.order;

import com.example.entity.Order;
import com.example.vo.order.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    Order getById(Long id);

    Order getByOrderNo(String orderNo);

    Order getPendingByProductId(Long productId);

    void insert(Order order);

    void updateStatus(@Param("id") Long id,
                      @Param("status") String status);

    void updateRejectReason(@Param("id") Long id,
                            @Param("status") String status,
                            @Param("rejectReason") String rejectReason);

    void updateCancelReason(@Param("id") Long id,
                            @Param("status") String status,
                            @Param("cancelReason") String cancelReason);

    void updateCompleted(@Param("id") Long id,
                         @Param("status") String status);

    OrderVO getOrderVOById(Long id);

    List<OrderVO> listByBuyer(@Param("buyerId") Long buyerId,
                              @Param("status") String status,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    Long countByBuyer(@Param("buyerId") Long buyerId,
                      @Param("status") String status);

    List<OrderVO> listBySeller(@Param("sellerId") Long sellerId,
                               @Param("status") String status,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    Long countBySeller(@Param("sellerId") Long sellerId,
                       @Param("status") String status);
}