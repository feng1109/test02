package com.eseasky.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.dto.SysUserOrgTreeDTO;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.vo.OrderMeetingVO;
import com.eseasky.modules.order.vo.request.*;
import com.eseasky.modules.order.vo.response.*;

import java.util.List;

/**
 * <p>
 * 会议室预约订单 服务类
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
public interface OrderMeetingService extends IService<OrderMeetingList> {

    /**
     * 功能描述: <br>
     * 〈创建订单〉
     * @Param: [orderMeetingVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/8 10:49
     */
    R<OrderMeetingList> createOrder(OrderMeetingVO orderMeetingVO);

    /**
     * 功能描述: <br>
     * 〈审批后修改订单信息〉
     * @Param: [orderMeetingVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/9 11:22
     */
    R<String> approve(String orderMeetingId,Integer state);

    /**
     * 功能描述: <br>
     * 〈获取用户会议室预约订单〉
     * @Param: [userListReqVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/9 14:24
     */
    R<ListWithPage<UserMeetingListRepVO>> getMeetingOrderPage(UserMeetingOrderReqVO userMeetingOrderReqVO);

    /**
     * 功能描述: <br>
     * 〈查看订单详情〉
     * @Param: [orderSeatId]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/9 14:58
     */
    R<OrderMeetingListDetailRepVO> getMeetingDetail(MeetingDetailReqVO meetingDetailReqVO);

    /**
     * 功能描述: <br>
     * 〈查看打卡页面详情〉
     * @Param: [orderId]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/9 15:24
     */
    R<List<MeetingClockRepVO>> showClockInfo(MeetingClockReqVO meetingClockReqVO);

    /**
     * 功能描述: <br>
     * 〈取消会议室预约〉
     * @Param: [cancelMeetingOrderReqVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/9 16:51
     */
    R<String> cancelOrder(CancelMeetingOrderReqVO cancelMeetingOrderReqVO);

    /**
     * 功能描述: <br>
     * 〈会议室预约签到〉
     * @Param: [arriveOrderReqVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/10 9:14
     */
    R<String> arriveOrder(ArriveMeetingOrderReqVO arriveMeetingOrderReqVO);

    /**
     * 功能描述: <br>
     * 〈获取签到明细〉
     * @Param: [longDetailReqVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/10 16:56
     */
    R<ListWithPage<OrderMeetingRentDetailRepVO>> showMeetingRentDetail(MeetingClockDetailReqVO longDetailReqVO);

    /**
     * 功能描述: <br>
     * 〈开启或者关闭会议〉
     * @Param: [openCloseMeetingReqVO]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/16 17:59
     */
    R<String> openOrCloseMeeting(OpenCloseMeetingReqVO openCloseMeetingReqVO);

    /**
     * 会议室记录
     *
     * @param userMeetingListReqVO
     * @return
     */
    R<ListWithPage<UserMeetingInfoRepVO>> getMeetingRecord(UserMeetingListReqVO userMeetingListReqVO);

    /**
     * 删除空间时处理订单
     * @param idList
     * @param spaceType
     * @return
     */
    R<String> delSpace(List<String> idList,Integer spaceType);


    /**
     * 获取组织及用户树形结构数据
     * @return
     */
    R<List<SysUserOrgTreeDTO>> getTreeUsers();

    /**
     * 根据是否时组织和id来查询下面的用户信息
     * @return
     */
    R<List<SysUserOrgTreeDTO>> getTreeUsersByIsOrgAndId(SysUserOrgTreeReqVO sysUserOrgTreeReqVO);

    /**
     * 获取单次预约详情
     * @param orderMeetingId
     * @return
     */
    R<OrderMeetingShortDetailRepVO> getShortDetail(String orderMeetingId);

    /**
     * 获取长租详情
     * @param orderMeetingId
     * @return
     */
    R<OrderMeetingLongDetailRepVO> getLongDetail(String orderMeetingId);
}
