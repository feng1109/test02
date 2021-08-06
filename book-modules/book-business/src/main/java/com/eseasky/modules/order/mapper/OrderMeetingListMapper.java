package com.eseasky.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.order.dto.OrderMeetingDetailDTO;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.vo.request.UserMeetingOrderReqVO;
import com.eseasky.modules.order.vo.response.MeetingClockRepVO;
import com.eseasky.modules.order.vo.response.OrderMeetingListDetailRepVO;
import com.eseasky.modules.order.vo.response.UserMeetingListRepVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会议室预约订单 Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Repository
public interface OrderMeetingListMapper extends BaseMapper<OrderMeetingList> {

    /**
     * 功能描述: <br>
     * 〈查询某一用户某一时间段里是否已存在订单防止重复〉
     *
     * @Param: [userId, orderStartTime, orderEndTime]
     * @Return: java.lang.Integer
     * @Author: 王鹏滔
     * @Date: 2021/6/8 15:08
     */
    Integer getExistOrderCount(@Param("userId") String userId, @Param("startTime") Date orderStartTime, @Param("endTime") Date orderEndTime, @Param("stateList") List<Integer> stateList, @Param("orderType") Integer orderType);


    /**
     * 功能描述: <br>
     * 〈查看房间是否已经被预约〉
     *
     * @Param: [orderMeeting]
     * @Return: boolean
     * @Author: 王鹏滔
     * @Date: 2021/6/8 15:47
     */
    Integer getRoomRepeat(@Param("roomId") String roomId, @Param("startTime") Date orderStartTime, @Param("endTime") Date orderEndTime, @Param("stateList") List<Integer> stateList);

    /**
     * 功能描述: <br>
     * 〈查看用户预约订单〉
     *
     * @Param: [page, userListReqVO]
     * @Return: java.util.List<com.eseasky.modules.order.vo.response.UserRentListVO>
     * @Author: 王鹏滔
     * @Date: 2021/6/9 14:30
     */
    List<UserMeetingListRepVO> getDataList(@Param("vo") UserMeetingOrderReqVO userMeetingOrderReqVO);


    /**
     * 查询会议室订单总数
     *
     * @param userMeetingOrderReqVO
     * @return
     */
    Integer getCount(@Param("vo") UserMeetingOrderReqVO userMeetingOrderReqVO);

    /**
     * 功能描述: <br>
     * 〈获取订单详情〉
     *
     * @Param: [orderId]
     * @Return: com.eseasky.modules.order.vo.response.OrderMeetingInfoVO
     * @Author: 王鹏滔
     * @Date: 2021/6/9 15:02
     */
    OrderMeetingListDetailRepVO getOrderDetail(@Param("orderId") String orderId);

    /**
     * 功能描述: <br>
     * 〈〉
     *
     * @Param: [showClockReqVO]
     * @Return: com.eseasky.modules.order.vo.response.ShowClockRepVO
     * @Author: 王鹏滔
     * @Date: 2021/6/9 15:26
     */
    List<MeetingClockRepVO> getClockInfo(@Param("ids") List<String> orderMeetingIds, @Param("startTime") Date date);

    /**
     * 根据id集合获取订单详情集合
     *
     * @param meetingIds
     * @return
     */
    List<UserMeetingListRepVO> getOrderMeetingVoByIds(@Param("ids") List<String> meetingIds);

    /**
     * 获取某段日期的短租数据
     *
     * @param orderMeetingIds
     * @param startTime
     * @param endTime
     * @param states
     * @param orderType
     * @return
     */
    List<MeetingClockRepVO> getShortClockInfoByDate(@Param("ids") List<String> orderMeetingIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("states") List<Integer> states, @Param("orderType") Integer orderType);

    /**
     * 获取最新的短租数据日期
     *
     * @param orderMeetingIds
     * @param states
     * @param orderType
     * @return
     */
    Date getShortLatestDate(@Param("ids") List<String> orderMeetingIds, @Param("states") List<Integer> states, @Param("orderType") Integer orderType);

    /**
     * 获取长租的所有信息
     *
     * @param orderMeetingIds
     * @param states
     * @return
     */
    List<MeetingClockRepVO> getLongClockInfoByDate(@Param("ids") List<String> orderMeetingIds, @Param("states") List<Integer> states, @Param("orderType") Integer orderType);

    /**
     * 获取单词预约详情
     *
     * @param orderMeetingId
     */
    OrderMeetingDetailDTO getMeetingDetail(@Param("orderMeetingId") String orderMeetingId,@Param("orderType") Integer orderType);
}
