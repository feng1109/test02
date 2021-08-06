package com.eseasky.modules.order.mapper;

import com.eseasky.modules.order.entity.OrderMeetingRentDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.order.vo.request.MeetingClockReqVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2021-06-17
 */
@Repository
public interface OrderMeetingRentDetailMapper extends BaseMapper<OrderMeetingRentDetail> {

    /**
     * 查看打卡记录总数
     * @param meetingClockReqVO
     * @return
     */
    Integer getCount(@Param("vo") MeetingClockReqVO meetingClockReqVO);

    /**
     * 获取打卡记录
     * @param meetingClockReqVO
     * @return
     */
    List<OrderMeetingRentDetail> getClockDataList(@Param("vo") MeetingClockReqVO meetingClockReqVO);
}
