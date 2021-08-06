package com.eseasky.modules.order.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.common.rabbitmq.dto.OrderMQDTO;
import com.eseasky.common.rabbitmq.message.RabbitMqUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.InsertApproveInfoDTO;
import com.eseasky.modules.order.dto.OrderNoticeDTO;
import com.eseasky.modules.order.dto.SendNoticeDTO;
import com.eseasky.modules.order.entity.OrderApprove;
import com.eseasky.modules.order.entity.OrderApprover;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.mapper.OrderApproveMapper;
import com.eseasky.modules.order.mapper.OrderApproverMapper;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.service.OrderApproveService;
import com.eseasky.modules.order.service.OrderLockService;
import com.eseasky.modules.order.service.OrderMeetingService;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.utils.OrderNoticeUtils;
import com.eseasky.modules.order.utils.OrderUtils;
import com.eseasky.modules.order.vo.request.ApproveAgreeReqVO;
import com.eseasky.modules.order.vo.request.ApproveRejectReqVO;
import com.eseasky.modules.order.vo.request.ShowApproveListReqVO;
import com.eseasky.modules.order.vo.response.OrderMeetingListDetailRepVO;
import com.eseasky.modules.order.vo.response.ShowApproveListRepVO;
import com.eseasky.modules.order.vo.response.StaLongRepVO;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批 服务实现类
 * </p>
 *
 * @author
 * @since 2021-06-11
 */
@Service
@Transactional
public class OrderApproveServiceImpl extends ServiceImpl<OrderApproveMapper, OrderApprove> implements OrderApproveService {


    @Autowired
    OrderApproverMapper orderApproverMapper;

    @Autowired
    OrderApproveMapper orderApproveMapper;

    @Autowired
    OrderApproverServiceImpl orderApproverService;

    @Autowired
    OrderLockService orderLockService;

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    OrderMeetingListMapper orderMeetingListMapper;

    @Autowired
    OrderMeetingService orderMeetingService;

    @Autowired
    OrderNoticeService orderNoticeService;

    @Autowired
    SpaceSeatService spaceSeatService;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Autowired
    OrderUtils orderUtils;

    /**
     * @return
     * @description: 插入审批数据
     * @author: lc
     * @date: 2021/6/24 11:20
     * @params
     */
    @Override
    public void insertApproveInfo(InsertApproveInfoDTO insertApproveInfoDTO) {
        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();

        // 获取传参参数
        Date orderStartTime = insertApproveInfoDTO.getOrderStartTime();
        Integer orderType = insertApproveInfoDTO.getOrderType();
        String listId = insertApproveInfoDTO.getOrderListId();

        // 插入审批数据
        OrderApprove orderApprove = JSONObject.parseObject(JSON.toJSONString(insertApproveInfoDTO), OrderApprove.class);
        orderApprove.setApproveState(OrderConstant.ApproveState.WAIT);
        orderApproveMapper.insert(orderApprove);
        Date nowDate = new Date();

        // 获取审批id
        String approveId = orderApprove.getApproveId();

        // 插入审批人数据
        String[] approvers = insertApproveInfoDTO.getApprovers();
        List<OrderApprover> collect = Arrays.stream(approvers).map(el -> {
            OrderApprover orderApprover = new OrderApprover()
                    .setApproveId(approveId)
                    .setUserId(el);
            return orderApprover;
        }).collect(Collectors.toList());
        orderApproverService.saveBatch(collect);

        // 消息队列：审批失效
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("tenantCode", tenantCode);
        orderMap.put("approveId", approveId);
        orderMap.put("listId",listId);
        orderMap.put("orderType",orderType);

        // 审批失效时间
        String time="";
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)){
            time=StrUtil.toString(DateUtil.betweenMs(nowDate,orderStartTime));
        }else {
            time=StrUtil.toString(DateUtil.betweenMs(nowDate,DateUtil.endOfDay(orderStartTime)));
        }

        orderMQDTO.setTime(time)
                .setRoutingKey(OrderConstant.routeKey.APPROVE_OFF_ROUTINGKEY)
                .setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderMQDTO);
    }

    /**
     * @return
     * @description: 查看审批数据
     * @author: lc
     * @date: 2021/6/11 17:21
     * @params
     */
    @Override
    public R showApproveList(ShowApproveListReqVO showApproveListReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        // 获取审批列表
        Page<ShowApproveListRepVO> page = new Page<>(showApproveListReqVO.getPageNum(), showApproveListReqVO.getPageSize());
        List<ShowApproveListRepVO> approveList = orderApproveMapper.getApproveList(page, userId,showApproveListReqVO);

        page.setRecords(approveList);
        return R.ok(page);
    }

    /**
     * @description: 同意
     * @author: lc
     * @date: 2021/6/24 15:43
     * @params
     * @return
     */
    @Override
    public R agreeApproveList(ApproveAgreeReqVO approveAgreeReqVO) {

        String approveId = approveAgreeReqVO.getApproveId();

        // 审批订单状态置为同意
        OrderApprove orderApprove= (OrderApprove)orderLockService.approveAgree(approveId).getData();

        // 获取订单信息
        Integer orderType = orderApprove.getOrderType();
        String listId = orderApprove.getOrderListId();

        // 订单类型为单人长租，将订单状态置为待签到
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.WAIT_ARRIVE)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            orderSeatListMapper.update(null, updateWrapper);
        }

        // 订单类型为会议室订单，将订单状态置为待签到
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING,OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            orderMeetingService.approve(listId,OrderConstant.MeetingStateEnum.TO_START.getAftId());
        }
        sendNotice(orderType,listId,true);
        return R.ok("审批成功");
    }

    /**
     * @description: 驳回审批
     * @author: lc
     * @date: 2021/6/24 17:18
     * @params
     * @return
     */
    @Override
    public R rejectApproveList(ApproveRejectReqVO approveRejectReqVO) {
        String approveId = approveRejectReqVO.getApproveId();

        // 审批订单状态置为未通过
        OrderApprove orderApprove= (OrderApprove)orderLockService.approveReject(approveId).getData();

        // 获取订单信息
        Integer orderType = orderApprove.getOrderType();
        String listId = orderApprove.getOrderListId();

        // 将订单状态置为被驳回
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.APPROVE_REJECT)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            orderSeatListMapper.update(null, updateWrapper);
        }

        // 订单类型为会议室订单，将订单状态置为审核不通过
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING,OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            orderMeetingService.approve(listId,OrderConstant.MeetingStateEnum.APPROVE_FAILED.getAftId());
        }
        sendNotice(orderType,listId,false);
        return R.ok("审批成功");
    }

    private void sendNotice(Integer orderType, String listId,Boolean appointmentResult) {
        SendNoticeDTO noticeApp = null;
        // 订单类型为单人长租，将订单状态置为待签到
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            //添加通知
            StaLongRepVO longDetail = orderSeatListMapper.getLongDetail(listId);
            String noticeContent;
            String approveState;
            if (appointmentResult){
                approveState = OrderConstant.ApproveState.AGREE.toString();
                noticeContent = "座位预约审核通过";
            } else {
                approveState = OrderConstant.ApproveState.REJECT.toString();
                noticeContent = "座位预约审核未通过";
            }
            OrderNoticeDTO orderNoticeDTO = OrderUtils.getOrderNoticeMeetingDTOBySeatList(longDetail,noticeContent,approveState,orderType);
            noticeApp = OrderNoticeUtils.getSendNoticeDTO(longDetail.getUserId(), OrderConstant.NoticeType.App, OrderConstant.NoticeTitleType.SEAT,OrderConstant.NoticeContentType.APPROVE,"预约审核通知", JSONObject.toJSONString(orderNoticeDTO));
        }

        // 订单类型为会议室订单，将订单状态置为待签到
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            //添加通知
            String noticeContent;
            String approveState;
            if (appointmentResult){
                approveState = OrderConstant.ApproveState.AGREE.toString();
                noticeContent = "会议室预约审核通过";
            } else {
                approveState = OrderConstant.ApproveState.REJECT.toString();
                noticeContent = "会议室预约审核未通过";
            }
            OrderMeetingListDetailRepVO meetingDetail = orderMeetingListMapper.getOrderDetail(listId);
            OrderNoticeDTO orderNoticeDTO = OrderUtils.getOrderNoticeDTOByMeetingList(meetingDetail,noticeContent,approveState);
            noticeApp = OrderNoticeUtils.getSendNoticeDTO(meetingDetail.getUserId(), OrderConstant.NoticeType.App, OrderConstant.NoticeTitleType.MEETING,OrderConstant.NoticeContentType.APPROVE,"预约审核通知", JSONObject.toJSONString(orderNoticeDTO));
            //审核通过
            if (appointmentResult) {
                //审核通过 添加参会人邀请通过通知
                orderUtils.sendNoticeToAppointmentPeople(meetingDetail.getAttendMeetingPeopleStr(),meetingDetail.getOrderMeetingInfoId(),meetingDetail);
            }
        }
        if (ObjectUtil.isNotNull(noticeApp)) {
            orderNoticeService.sendNotice(noticeApp);
        }
    }
}
