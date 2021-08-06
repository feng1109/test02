package com.eseasky.modules.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.utils.WxUtils;
import com.eseasky.common.code.wx.MsgObject;
import com.eseasky.common.dao.SysUserMapper;
import com.eseasky.common.entity.SysUser;
import com.eseasky.common.rabbitmq.message.RabbitMqUtils;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.entity.OrderNotice;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.service.OrderMeetingMQService;
import com.eseasky.modules.order.service.OrderMeetingService;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.service.OrderViolateDetailService;
import com.eseasky.modules.order.utils.OrderNoticeUtils;
import com.eseasky.modules.order.utils.OrderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @describe: 会议室预约消息队列处理服务层
 * @title: OrderMQServiceImpl
 * @Author lc
 * @Date: 2021/4/26
 */
@Slf4j
@Service
public class OrderMeetingMQServiceImpl implements OrderMeetingMQService {

    @Resource
    OrderMeetingListMapper orderMeetingListMapper;

    @Resource
    SysUserMapper sysUserMapper;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    OrderViolateDetailService orderViolateDetailService;

    @Autowired
    OrderNoticeService orderNoticeService;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Autowired
    OrderMeetingService orderMeetingService;

    @Autowired
    OrderUtils orderUtils;

    @Autowired
    WxUtils wxUtils;

    /**
     * 功能描述: <br>
     * 〈取消订单〉
     *
     * @Param: [msg]
     * @Return: void
     * @Author: 王鹏滔
     * @Date: 2021/6/16 16:40
     */
    @Override
    public void cancelOrder(Map<String, Object> msg) {
        //会议室订单审核通过后，人数不达标，将其取消
        String orderMeetingId = StrUtil.toString(msg.get("orderMeetingId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("OrderMeetingMQServiceImpl.cancelOrder 传参信息有误：" + msg);
            return;
        }
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);

        try {
            LambdaQueryWrapper<OrderMeetingList> meetingLambdaQueryWrapper = new LambdaQueryWrapper<>();
            meetingLambdaQueryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
            OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(meetingLambdaQueryWrapper);
            if (ObjectUtil.isNull(orderMeetingList)) {
                log.info("OrderMeetingMQServiceImpl.cancelOrder 不存在此订单，orderMeetingId：" + orderMeetingId);
                return;
            }
            if (!OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
                log.info("OrderMeetingMQServiceImpl.cancelOrder 非短租订单，不能取消");
                return;
            }
            Date nowDate = new Date();
            //如果还是待开始状态，则将其取消
            if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(orderMeetingList.getState())) {
                LambdaUpdateWrapper<OrderMeetingList> meetingLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                meetingLambdaUpdateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.SYSTEM_CANCEL.getAftId())
                        .set(OrderMeetingList::getUseEndTime, nowDate)
                        .set(OrderMeetingList::getUpdateTime, nowDate)
                        .eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
                orderMeetingListMapper.update(null, meetingLambdaUpdateWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

    @Override
    public void finishOrder(Map<String, Object> msg) {
        // 获取传参
        String orderMeetingId = StrUtil.toString(msg.get("orderMeetingId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("OrderMeetingMQServiceImpl.finishOrder 传参信息有误：" + msg);
            return;
        }
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            LambdaQueryWrapper<OrderMeetingList> meetingLambdaQueryWrapper = new LambdaQueryWrapper<>();
            meetingLambdaQueryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
            OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(meetingLambdaQueryWrapper);
            if (ObjectUtil.isNull(orderMeetingList)) {
                log.info("OrderMeetingMQServiceImpl.finishOrder 不存在此订单，orderMeetingId：" + orderMeetingId);
                return;
            }
            Integer state = orderMeetingList.getState();
            Date nowDate = new Date();
            //待审核，待开始，进行中 状态将其改为结束
            if (Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId(), OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId()).contains(state)) {
                LambdaUpdateWrapper<OrderMeetingList> meetingLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                meetingLambdaUpdateWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
                meetingLambdaUpdateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.FINISH.getAftId());
                meetingLambdaUpdateWrapper.set(OrderMeetingList::getUseEndTime, nowDate);
                meetingLambdaUpdateWrapper.set(OrderMeetingList::getUpdateTime, nowDate);
                orderMeetingListMapper.update(null, meetingLambdaUpdateWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

    @Override
    public void clockReminderMeetingMessage(Map<String, Object> msg) {
        // 获取传参
        String orderMeetingId = StrUtil.toString(msg.get("orderMeetingId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("OrderMeetingMQServiceImpl.clockReminderMeetingMessage 传参信息有误：" + msg);
            return;
        }
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            LambdaQueryWrapper<OrderMeetingList> meetingLambdaQueryWrapper = new LambdaQueryWrapper<>();
            meetingLambdaQueryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
            OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(meetingLambdaQueryWrapper);
            if (ObjectUtil.isNull(orderMeetingList)) {
                log.info("OrderMeetingMQServiceImpl.clockReminderMeetingMessage 不存在此订单，orderMeetingId：" + orderMeetingId);
                return;
            }

            //待开始和进行中状态的通知打卡
            if (Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(),OrderConstant.MeetingStateEnum.IN_USE.getAftId()).contains(orderMeetingList.getState())){
                //批量通知打卡
                //1.获取到参会人id集合
                List<String> userIds = orderUtils.getUserIds(orderMeetingList.getAttendMeetingPeople(), orderMeetingList.getOrderMeetingInfoId());

                if (CollectionUtil.isNotEmpty(userIds)) {
                    LambdaQueryWrapper<SysUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    userLambdaQueryWrapper.in(SysUser::getId, userIds);
                    List<SysUser> userList = sysUserMapper.selectList(userLambdaQueryWrapper);

                    List<OrderNotice> noticeList = new ArrayList<>();
                    if (CollectionUtil.isNotEmpty(userList)) { //批量发送消息
                        for (SysUser user : userList) {
                            JSONObject meetingClockWxMsg = orderUtils.getClockWxMsg(user, orderMeetingList, "会议室打卡");
                            log.info("微信通知传参："+JSONObject.toJSONString(meetingClockWxMsg));
                            //发送微信通知消息
                            if (StringUtils.isNotBlank(user.getOpenid())) {
                                MsgObject result = wxUtils.sendMsg(meetingClockWxMsg);
                                if (0 != result.getErrcode() && !"ok".equalsIgnoreCase(result.getErrmsg())) {
                                    log.error("OrderMeetingMQServiceImpl.clockReminderMeetingMessage 发送微信通知错误  msg：" + result.getErrmsg());
                                    continue;
                                }
                                //wx通知
                                OrderNotice noticeWechat = OrderNoticeUtils.getOrderNotice(user.getId(), OrderConstant.NoticeType.WeChat, OrderConstant.NoticeTitleType.MEETING,null, "会议室打卡", JSONObject.toJSONString(meetingClockWxMsg));
                                noticeList.add(noticeWechat);
                            }
                        }
                    }
                    if (CollectionUtil.isNotEmpty(noticeList)) {
                        orderNoticeService.saveBatch(noticeList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }
}
