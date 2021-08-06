package com.eseasky.modules.order.mqlistener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.HandleBlacklistDTO;
import com.eseasky.modules.order.service.ApproveMQSerVice;
import com.eseasky.modules.order.service.GroupMQService;
import com.eseasky.modules.order.service.OrderMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @describe: 预约模块延时队列监听
 * @title: orderListener
 * @Author lc
 * @Date: 2021/4/26
 */
@Slf4j
@Component
public class OrderListener {


    @Autowired
    OrderMQService orderMQService;

    @Autowired
    GroupMQService groupMQService;

    @Autowired
    ApproveMQSerVice approveMQSerVice;
    /**
     * @description: 迟到超时，将订单状态改为未签到
     * @author: lc
     * @date: 2021/4/26 10:36
     * @params [msg]
     * @return void
     */
    @RabbitListener(queues = OrderConstant.queue.ORDER_LATE_QUEUE)
    public void consumeLateMessage(Map<String,Object> msg) {
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.handleLateList(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.ORDER_LEAVE_QUEUE)
    public void consumeLeaveMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.handleLeaveList(msg);}

    @RabbitListener(queues = OrderConstant.queue.ORDER_WAIT_LEAVE_QUEUE)
    public void consumeWaitLeaveMessage(Map<String,Object> msg) {
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.handleWaitLeaveList(msg);
    }

    /**
     *处理是否进入黑名单
     */
    @RabbitListener(queues = OrderConstant.queue.BLACK_LIST_QUEUE)
    public void consumerHandleBLMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        HandleBlacklistDTO handleBlacklistDTO = JSONObject.parseObject(JSON.toJSONString(msg), HandleBlacklistDTO.class);
        orderMQService.handleBlackList(handleBlacklistDTO);
    }

    /**
     *处理黑名单过期
     */
    @RabbitListener(queues = OrderConstant.queue.OUT_BLACK_LIST_QUEUE)
    public void consumerOutBLMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.handleOutBlackList(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.FINISH_LONG_QUEUE)
    public void consumeFinishLongMessage(Map<String,Object> msg) {
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.handleFinishLong(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.AWAY_BACK_QUEUE)
    public void consumerAwayBackMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.handleAwayBack(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.GROUP_OFF_QUEUE)
    public void consumerGroupOffMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        groupMQService.handleGroupOff(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.GROUP_ARRIVE_QUEUE)
    public void consumerGroupArriveMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        groupMQService.handleGroupArrive(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.GROUP_WAIT_LEAVE_QUEUE)
    public void consumerGroupWaitLeaveMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        groupMQService.handleGroupWaitLeave(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.GROUP_LEAVE_QUEUE)
    public void consumerGroupLeaveMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        groupMQService.handleGroupLeave(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.APPROVE_OFF_QUEUE)
    public void consumerApproveOffMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        approveMQSerVice.handleApproveOff(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.CLOCK_REMINDER_QUEUE)
    public void clockReminderMessage(Map<String,Object> msg){
        log.info("MQ传参信息-->"+StrUtil.toString(msg));
        orderMQService.clockReminderMessage(msg);
    }
}