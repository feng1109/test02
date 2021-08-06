package com.eseasky.modules.order.service;

import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.entity.OrderMeetingList;

/**
 * 使用分布式锁创建订单
 * @Author: 王鹏滔
 * @Date: 2021/6/8 15:40
 */
public interface OrderMeetingLockService {
    /**
     * 功能描述: <br>
     * 〈对生成订单过程加分布式锁，生成订单前，去数据库查询是否有冲突的订单，若没有则生成订单〉
     * @Param: [orderMeeting, tenantCode]
     * @Return: void
     * @Author: 王鹏滔
     * @Date: 2021/6/8 15:42
     */
    R lockCreatOrder(OrderMeetingList orderMeetingList, String tenantCode);

    /**
     * 功能描述: <br>
     * 〈取消订单添加分布式锁〉
     * @Param: [id, orderType, tenantCode, userId, signAdvanceTime, nowDate]
     * @Return: com.eseasky.common.code.utils.R
     * @Author: 王鹏滔
     * @Date: 2021/6/9 17:40
     */
    R lockCancelOrder(String id, String tenantCode, String userId);

}
