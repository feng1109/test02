package com.eseasky.modules.order.service;

import java.util.Map;

public interface OrderMeetingMQService {


    /** 取消订单*/
    void cancelOrder(Map<String,Object> msg);

    /** 完成订单*/
    void finishOrder(Map<String, Object> msg);

    /**
     * 打卡通知
     * @param msg
     */
    void clockReminderMeetingMessage(Map<String, Object> msg);
}
