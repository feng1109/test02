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
 * ?????? ???????????????
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
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/6/24 11:20
     * @params
     */
    @Override
    public void insertApproveInfo(InsertApproveInfoDTO insertApproveInfoDTO) {
        // ??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();

        // ??????????????????
        Date orderStartTime = insertApproveInfoDTO.getOrderStartTime();
        Integer orderType = insertApproveInfoDTO.getOrderType();
        String listId = insertApproveInfoDTO.getOrderListId();

        // ??????????????????
        OrderApprove orderApprove = JSONObject.parseObject(JSON.toJSONString(insertApproveInfoDTO), OrderApprove.class);
        orderApprove.setApproveState(OrderConstant.ApproveState.WAIT);
        orderApproveMapper.insert(orderApprove);
        Date nowDate = new Date();

        // ????????????id
        String approveId = orderApprove.getApproveId();

        // ?????????????????????
        String[] approvers = insertApproveInfoDTO.getApprovers();
        List<OrderApprover> collect = Arrays.stream(approvers).map(el -> {
            OrderApprover orderApprover = new OrderApprover()
                    .setApproveId(approveId)
                    .setUserId(el);
            return orderApprover;
        }).collect(Collectors.toList());
        orderApproverService.saveBatch(collect);

        // ???????????????????????????
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("tenantCode", tenantCode);
        orderMap.put("approveId", approveId);
        orderMap.put("listId",listId);
        orderMap.put("orderType",orderType);

        // ??????????????????
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
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/6/11 17:21
     * @params
     */
    @Override
    public R showApproveList(ShowApproveListReqVO showApproveListReqVO) {

        // ??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        // ??????????????????
        Page<ShowApproveListRepVO> page = new Page<>(showApproveListReqVO.getPageNum(), showApproveListReqVO.getPageSize());
        List<ShowApproveListRepVO> approveList = orderApproveMapper.getApproveList(page, userId,showApproveListReqVO);

        page.setRecords(approveList);
        return R.ok(page);
    }

    /**
     * @description: ??????
     * @author: lc
     * @date: 2021/6/24 15:43
     * @params
     * @return
     */
    @Override
    public R agreeApproveList(ApproveAgreeReqVO approveAgreeReqVO) {

        String approveId = approveAgreeReqVO.getApproveId();

        // ??????????????????????????????
        OrderApprove orderApprove= (OrderApprove)orderLockService.approveAgree(approveId).getData();

        // ??????????????????
        Integer orderType = orderApprove.getOrderType();
        String listId = orderApprove.getOrderListId();

        // ????????????????????????????????????????????????????????????
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.WAIT_ARRIVE)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            orderSeatListMapper.update(null, updateWrapper);
        }

        // ???????????????????????????????????????????????????????????????
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING,OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            orderMeetingService.approve(listId,OrderConstant.MeetingStateEnum.TO_START.getAftId());
        }
        sendNotice(orderType,listId,true);
        return R.ok("????????????");
    }

    /**
     * @description: ????????????
     * @author: lc
     * @date: 2021/6/24 17:18
     * @params
     * @return
     */
    @Override
    public R rejectApproveList(ApproveRejectReqVO approveRejectReqVO) {
        String approveId = approveRejectReqVO.getApproveId();

        // ?????????????????????????????????
        OrderApprove orderApprove= (OrderApprove)orderLockService.approveReject(approveId).getData();

        // ??????????????????
        Integer orderType = orderApprove.getOrderType();
        String listId = orderApprove.getOrderListId();

        // ??????????????????????????????
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.APPROVE_REJECT)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            orderSeatListMapper.update(null, updateWrapper);
        }

        // ?????????????????????????????????????????????????????????????????????
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING,OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            orderMeetingService.approve(listId,OrderConstant.MeetingStateEnum.APPROVE_FAILED.getAftId());
        }
        sendNotice(orderType,listId,false);
        return R.ok("????????????");
    }

    private void sendNotice(Integer orderType, String listId,Boolean appointmentResult) {
        SendNoticeDTO noticeApp = null;
        // ????????????????????????????????????????????????????????????
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            //????????????
            StaLongRepVO longDetail = orderSeatListMapper.getLongDetail(listId);
            String noticeContent;
            String approveState;
            if (appointmentResult){
                approveState = OrderConstant.ApproveState.AGREE.toString();
                noticeContent = "????????????????????????";
            } else {
                approveState = OrderConstant.ApproveState.REJECT.toString();
                noticeContent = "???????????????????????????";
            }
            OrderNoticeDTO orderNoticeDTO = OrderUtils.getOrderNoticeMeetingDTOBySeatList(longDetail,noticeContent,approveState,orderType);
            noticeApp = OrderNoticeUtils.getSendNoticeDTO(longDetail.getUserId(), OrderConstant.NoticeType.App, OrderConstant.NoticeTitleType.SEAT,OrderConstant.NoticeContentType.APPROVE,"??????????????????", JSONObject.toJSONString(orderNoticeDTO));
        }

        // ???????????????????????????????????????????????????????????????
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            //????????????
            String noticeContent;
            String approveState;
            if (appointmentResult){
                approveState = OrderConstant.ApproveState.AGREE.toString();
                noticeContent = "???????????????????????????";
            } else {
                approveState = OrderConstant.ApproveState.REJECT.toString();
                noticeContent = "??????????????????????????????";
            }
            OrderMeetingListDetailRepVO meetingDetail = orderMeetingListMapper.getOrderDetail(listId);
            OrderNoticeDTO orderNoticeDTO = OrderUtils.getOrderNoticeDTOByMeetingList(meetingDetail,noticeContent,approveState);
            noticeApp = OrderNoticeUtils.getSendNoticeDTO(meetingDetail.getUserId(), OrderConstant.NoticeType.App, OrderConstant.NoticeTitleType.MEETING,OrderConstant.NoticeContentType.APPROVE,"??????????????????", JSONObject.toJSONString(orderNoticeDTO));
            //????????????
            if (appointmentResult) {
                //???????????? ?????????????????????????????????
                orderUtils.sendNoticeToAppointmentPeople(meetingDetail.getAttendMeetingPeopleStr(),meetingDetail.getOrderMeetingInfoId(),meetingDetail);
            }
        }
        if (ObjectUtil.isNotNull(noticeApp)) {
            orderNoticeService.sendNotice(noticeApp);
        }
    }
}
