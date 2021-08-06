package com.eseasky.modules.order.mapper;

import com.eseasky.modules.order.entity.OrderMeetingInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.order.vo.request.UserMeetingOrderReqVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2021-06-11
 */
@Repository
public interface OrderMeetingInfoMapper extends BaseMapper<OrderMeetingInfo> {
    Integer batchInsert(@Param("list") List<OrderMeetingInfo> list);

    /**
     * 分页获取会议室预约时 获取订单详情条数
     * @param userMeetingOrderReqVO
     * @return
     */
    Integer getCount(@Param("vo") UserMeetingOrderReqVO userMeetingOrderReqVO);

    /**
     * 获取分页数据
     * @param userMeetingOrderReqVO
     * @return
     */
    List<OrderMeetingInfo> getDataList(@Param("vo") UserMeetingOrderReqVO userMeetingOrderReqVO);

    /**
     * 根据会议室订单id和用户id获取最新的会议室记录
     * @param orderMeetingId
     * @return
     */
    OrderMeetingInfo getLatestRecordByOrderMeetingIdAndUserId(@Param("orderMeetingId") String orderMeetingId);

    /**
     * 根据会议室记录id获取会议室记录
     * @param orderMeetingInfoId
     * @return
     */
    OrderMeetingInfo getOrderMeetingInfoByInfoId(@Param("orderMeetingInfoId") String orderMeetingInfoId);
}
