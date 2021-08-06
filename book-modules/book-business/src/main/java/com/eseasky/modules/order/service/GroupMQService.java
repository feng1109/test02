package com.eseasky.modules.order.service;

import java.util.Map;

public interface GroupMQService {

    /** 拼团截止*/
     void handleGroupOff(Map<String, Object> msg);

     /** 拼团签到*/
     void handleGroupArrive(Map<String,Object> msg);

     /** 拼团时间结束待签到*/
     void handleGroupWaitLeave(Map<String,Object> msg);

     /** 拼团处理未签退*/
     void handleGroupLeave(Map<String,Object> msg);

}
