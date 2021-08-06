package com.eseasky.modules.order.mqlistener;

import cn.hutool.core.util.StrUtil;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.service.OrderMeetingMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @describe: 会议室预约延时队列监听
 * @title: orderListener
 * @Author lc
 * @Date: 2021/4/26
 */
@Slf4j
@Component
public class OrderMeetingListener {


    @Autowired
    OrderMeetingMQService orderMeetingMQService;

    @RabbitListener(queues = OrderConstant.queue.CANCEL_MEETING_QUEUE)
    public void cancelMeetingMessage(Map<String, Object> msg) {
        log.info("MQ：" + OrderConstant.queue.CANCEL_MEETING_QUEUE + "传参信息-->" + StrUtil.toString(msg));
        orderMeetingMQService.cancelOrder(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.FINISH_MEETING_QUEUE)
    public void finishMeetingMessage(Map<String, Object> msg) {
        log.info("MQ：" + OrderConstant.queue.FINISH_MEETING_QUEUE + "传参信息-->" + StrUtil.toString(msg));
        orderMeetingMQService.finishOrder(msg);
    }

    @RabbitListener(queues = OrderConstant.queue.CLOCK_REMINDER_MEETING_QUEUE)
    public void clockReminderMeetingMessage(Map<String, Object> msg) {
        log.info("MQ：" + OrderConstant.queue.CLOCK_REMINDER_MEETING_QUEUE + "传参信息-->" + StrUtil.toString(msg));
        orderMeetingMQService.clockReminderMeetingMessage(msg);
    }
}