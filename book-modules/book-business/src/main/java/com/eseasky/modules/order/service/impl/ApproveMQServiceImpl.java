package com.eseasky.modules.order.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderApprove;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.mapper.OrderApproveMapper;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.service.ApproveMQSerVice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * @describe:
 * @title: ApproveMQServeiceImpl
 * @Author lc
 * @Date: 2021/7/4
 */
@Slf4j
@Service
public class ApproveMQServiceImpl implements ApproveMQSerVice {

    @Autowired
    OrderApproveMapper orderApproveMapper;

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    OrderMeetingListMapper orderMeetingListMapper;



    /**
     * @description: 审批截止时间将审批状态及订单状态置为失效
     * @author: lc
     * @date: 2021/7/4 15:50
     * @params
     * @return
     */
    @Override
    public void handleApproveOff(Map<String,Object> msg){
        // 获取传参
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String approveId = StrUtil.toString(msg.get("approveId"));
        String listId = StrUtil.toString(msg.get("listId"));
        Integer orderType = Convert.toInt(msg.get("orderType"));
        Date nowDate = new Date();

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);

        try {
            // 查看审批状态
            OrderApprove orderApprove = orderApproveMapper.selectById(approveId);
            Integer approveState = orderApprove.getApproveState();

            // 若订单状态不为待审批，则return
            if (!approveState.equals(OrderConstant.ApproveState.WAIT)){
                return;
            }

            // 将审批记录置为失效
            LambdaUpdateWrapper<OrderApprove> approveUpdateWrapper = new LambdaUpdateWrapper<>();
            approveUpdateWrapper.set(OrderApprove::getApproveState, OrderConstant.ApproveState.NO_EFFECTIVE)
                    .eq(OrderApprove::getApproveId,approveId);
            orderApproveMapper.update(null,approveUpdateWrapper);

            // 长租订单改变订单状态为审批失效
            if (orderType.equals(OrderConstant.OrderType.LONG_RENT)){
                LambdaUpdateWrapper<OrderSeatList> listLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                listLambdaUpdateWrapper.set(OrderSeatList::getListState,OrderConstant.listState.APPROVE_NO_EFFECTIVE)
                        .eq(OrderSeatList::getOrderSeatId,listId);
                orderSeatListMapper.update(null,listLambdaUpdateWrapper);
            }

            // 长租订单改变订单状态为审批失效
            if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING,OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
                LambdaUpdateWrapper<OrderMeetingList> listLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                listLambdaUpdateWrapper.set(OrderMeetingList::getState,OrderConstant.MeetingStateEnum.APPROVE_INVALID.getAftId())
                        .eq(OrderMeetingList::getOrderMeetingId,listId);
                orderMeetingListMapper.update(null,listLambdaUpdateWrapper);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }

        log.info("订单状态置为失效成功");
    }




}
