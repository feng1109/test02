package com.eseasky.modules.order.service;

import com.eseasky.modules.order.dto.HandleBlacklistDTO;

import java.util.Map;

public interface OrderMQService {

    /** 处理未签到订单*/
    void  handleLateList(Map<String,Object> msg);

    /** 处理未签退订单*/
    void handleLeaveList(Map<String,Object> msg);

    /** 处理待签退订单*/
    void handleWaitLeaveList(Map<String,Object> msg);

    /** 处理违规信息是否进入黑名单*/
    void handleBlackList(HandleBlacklistDTO handleBlacklistDTO);

    /** 黑名单到期解除*/
    void handleOutBlackList( Map<String,Object> msg);

    /** 长租订单到期改为已完成，并续约*/
    void handleFinishLong(Map<String,Object> msg);

    /** 暂离返回改为*/
    void handleAwayBack(Map<String,Object> msg);

    /** 打卡微信通知*/
    void clockReminderMessage(Map<String, Object> msg);
}
