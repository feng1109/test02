package com.eseasky.modules.order.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderMeetingInfo;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.entity.OrderMeetingRentDetail;
import com.eseasky.modules.order.entity.OrderMeetingUser;
import com.eseasky.modules.order.vo.OrderMeetingVO;

import java.util.Date;

public class OrderMeetingUtil {

    /**
     * 功能描述: <br>
     * 〈创建会议室预约，vo转entity〉
     *
     * @Param: [orderMeetingVO, userDTO]
     * @Return: com.eseasky.modules.order.entity.OrderMeeting
     * @Author: 王鹏滔
     * @Date: 2021/6/9 10:44
     */
    public static OrderMeetingList getOrderMeeting(OrderMeetingVO orderMeetingVO, SysUserDTO userDTO, String orderNo, Boolean notNeedApprove,String orgId,String orgName) {
        Date nowDate = new Date();
        String userId = userDTO.getId();
        // 生成订单编号
        OrderMeetingList orderMeetingList = new OrderMeetingList();
        orderMeetingList.setOrderMeetingId(IdUtil.simpleUUID());
        orderMeetingList.setCreateUserId(userId);
        orderMeetingList.setCreateUserName(userDTO.getUsername());
        orderMeetingList.setCreateTime(nowDate);
        orderMeetingList.setUpdateUserId(userId);
        orderMeetingList.setUpdateUserName(userDTO.getUsername());
        orderMeetingList.setUpdateTime(nowDate);
        orderMeetingList.setOrderNo(orderNo);
        orderMeetingList.setBuildId(orderMeetingVO.getBuildId());
        orderMeetingList.setFloorId(orderMeetingVO.getFloorId());
        orderMeetingList.setRoomId(orderMeetingVO.getRoomId());
        orderMeetingList.setUserId(userId);
        orderMeetingList.setUserName(userDTO.getUsername());
        orderMeetingList.setUserPhone(userDTO.getPhone());
        orderMeetingList.setUserNo(userDTO.getUserNo());
        orderMeetingList.setUserType(Integer.valueOf(String.valueOf(userDTO.getUserType())));
        orderMeetingList.setUserWechat("");
        orderMeetingList.setOrderType(orderMeetingVO.getOrderType());
        //初始化为待审核，审核通过在改为待开始
        orderMeetingList.setState(OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId());
        if (null != notNeedApprove && notNeedApprove) {
            orderMeetingList.setState(OrderConstant.MeetingStateEnum.TO_START.getAftId());
        }
        orderMeetingList.setUseTime(0L);
        if (orderMeetingVO.getOrderType().equals(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING)){
            orderMeetingList.setUseTime(orderMeetingVO.getOrderEndTime().getTime() - orderMeetingVO.getOrderStartTime().getTime());
        }
        orderMeetingList.setContinueCount(0);
        orderMeetingList.setRenewLimitTime(0);
        orderMeetingList.setIsAdvanceCancel(0);
        orderMeetingList.setIsRequireApproval(0);
        orderMeetingList.setIsLate(0);
        orderMeetingList.setIsAdvanceLeave(0);
        orderMeetingList.setOrderStartTime(orderMeetingVO.getOrderStartTime());
        orderMeetingList.setOrderEndTime(orderMeetingVO.getOrderEndTime());
//        // 计算长租订单结束时间
//        if (orderMeetingVO.getOrderType().equals(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING)) {
//            Date orderStartTime = orderMeetingVO.getOrderStartTime();
//            orderMeetingVO.setOrderEndTime(DateUtil.offsetDay(orderStartTime, 30));
//        }
        //实际起始时间暂为空
        orderMeetingList.setUseStartTime(null);
        orderMeetingList.setUseEndTime(null);
        orderMeetingList.setIsComment(0);
        orderMeetingList.setUserOrgId(orgId);
        orderMeetingList.setUserOrgName(orgName);
        orderMeetingList.setTheme(orderMeetingVO.getTheme());
        orderMeetingList.setAttendMeetingWay(orderMeetingVO.getAttendMeetingWay());
        orderMeetingList.setAttendMeetingPeople(orderMeetingVO.getAttendMeetingPeople().toString());
        orderMeetingList.setDelFlag("0");
        return orderMeetingList;
    }

    /**
     * 功能描述: <br>
     * 〈长租获取会议室预约详情〉
     *
     * @Param: [orderMeetingVO, userDTO, orderNo]
     * @Return: com.eseasky.modules.order.entity.OrderMeeting
     * @Author: 王鹏滔
     * @Date: 2021/6/16 17:16
     */
    public static OrderMeetingInfo getOrderMeetingInfo(OrderMeetingList orderMeetingList) {
        OrderMeetingInfo orderMeetingInfo = new OrderMeetingInfo();
        orderMeetingInfo.setOrderMeetingInfoId(IdUtil.simpleUUID());
        orderMeetingInfo.setOrderMeetingId(orderMeetingList.getOrderMeetingId());
        orderMeetingInfo.setUserId(orderMeetingList.getUserId());
        orderMeetingInfo.setUserName(orderMeetingList.getUserName());
        //短租的时候就设置为订单的开始时间结束时间
        orderMeetingInfo.setUserStartTime(null);
        orderMeetingInfo.setUseEndTime(null);
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())){
            orderMeetingInfo.setUserStartTime(orderMeetingList.getOrderStartTime());
            orderMeetingInfo.setUseEndTime(orderMeetingList.getOrderEndTime());
        }
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())){
            //点击开始会议的时候将其写入
            orderMeetingInfo.setUserStartTime(new Date());
            //点击结束会议的时候将其写入
            orderMeetingInfo.setUseEndTime(null);
        }
        orderMeetingInfo.setState(orderMeetingList.getState());
        orderMeetingInfo.setMeetingState(orderMeetingList.getState());
        orderMeetingInfo.setTheme(orderMeetingList.getTheme());
        orderMeetingInfo.setIsComment(0);
        orderMeetingInfo.setOrderType(orderMeetingList.getOrderType());
        orderMeetingInfo.setAttendMeetingPeople(orderMeetingList.getAttendMeetingPeople());
        orderMeetingInfo.setIsAppointPerson(1);
        orderMeetingInfo.setCreateTime(new Date());
        orderMeetingInfo.setUpdateTime(new Date());
        orderMeetingInfo.setAttendMeetingWay(orderMeetingList.getAttendMeetingWay());
        orderMeetingInfo.setDelFlag("0");
        return orderMeetingInfo;
    }

    /**
     * 功能描述: <br>
     * 〈获取会议室签到记录〉
     * @Param: []
     * @Return: com.eseasky.modules.order.entity.OrderMeetingRentDetail
     * @Author: 王鹏滔
     * @Date: 2021/6/17 9:29
     */
    public static OrderMeetingRentDetail getOrderMeetingRentDetail(String orderMeetingId, String orderMeetingInfoId, SysUserDTO user){
        //插入会议室预约详情（用户签到信息）
        OrderMeetingRentDetail orderMeetingRentDetail = new OrderMeetingRentDetail();
        orderMeetingRentDetail.setOrderMeetingInfoId(orderMeetingInfoId);
        orderMeetingRentDetail.setUserId(user.getId());
        orderMeetingRentDetail.setUserName(user.getUsername());
        orderMeetingRentDetail.setOrderMeetingId(orderMeetingId);
        orderMeetingRentDetail.setUseStartTime(new Date());
        orderMeetingRentDetail.setUseEndTime(new Date());
        orderMeetingRentDetail.setListUseTime(0);
        orderMeetingRentDetail.setUseDay(DateUtil.beginOfDay(new Date()).toJdkDate());
        orderMeetingRentDetail.setIsLeave(0);
        orderMeetingRentDetail.setCreateTime(new Date());
        orderMeetingRentDetail.setUpdateTime(new Date());
        orderMeetingRentDetail.setDelFlag("0");
        return orderMeetingRentDetail;
    }

    /**
     * 功能描述: <br>
     * 〈获取初始化的记录，非签到记录〉
     * @Param: []
     * @Return: com.eseasky.modules.order.entity.OrderMeetingRentDetail
     * @Author: 王鹏滔
     * @Date: 2021/6/17 9:29
     */
    public static OrderMeetingRentDetail getOrderMeetingRentDetailWithOutArrive(String orderMeetingId, String orderMeetingInfoId, String userId){
        //插入会议室预约详情（用户签到信息）
        OrderMeetingRentDetail orderMeetingRentDetail = new OrderMeetingRentDetail();
        orderMeetingRentDetail.setOrderMeetingInfoId(orderMeetingInfoId);
        orderMeetingRentDetail.setUserId(userId);
        orderMeetingRentDetail.setUserName("");
        orderMeetingRentDetail.setOrderMeetingId(orderMeetingId);
        orderMeetingRentDetail.setUseStartTime(null);
        orderMeetingRentDetail.setUseEndTime(null);
        orderMeetingRentDetail.setListUseTime(0);
        orderMeetingRentDetail.setUseDay(null);
        orderMeetingRentDetail.setIsLeave(0);
        orderMeetingRentDetail.setCreateTime(new Date());
        orderMeetingRentDetail.setUpdateTime(new Date());
        orderMeetingRentDetail.setDelFlag("0");
        return orderMeetingRentDetail;
    }

    public static OrderMeetingUser getOrderMeetingUser(String orderMeetingId, String orderMeetingInfoId,String userId) {
        OrderMeetingUser orderMeetingUser = new OrderMeetingUser();
        orderMeetingUser.setId(IdUtil.simpleUUID());
        orderMeetingUser.setOrderMeetingId(orderMeetingId);
        orderMeetingUser.setOrderMeetingInfoId(orderMeetingInfoId);
        orderMeetingUser.setUserId(userId);
        return orderMeetingUser;

    }
}
