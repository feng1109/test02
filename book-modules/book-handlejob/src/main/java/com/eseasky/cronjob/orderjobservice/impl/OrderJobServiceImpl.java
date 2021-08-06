package com.eseasky.cronjob.orderjobservice.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.cronjob.orderjobservice.OrderJobService;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderLongRentDetail;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @describe: 预约模块定时任务
 * @title: OrderCronJob
 * @Author lc
 * @Date: 2021/5/20
 */
@Service
public class OrderJobServiceImpl implements OrderJobService {


    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    /**
     * @description: 长租明细表未打卡记录置为待签到
     * @author: lc
     * @date: 2021/5/20 13:51
     * @params []
     * @return void
     */
    @Override
    public void finishLongDetail(String tenantCode){

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            LambdaUpdateWrapper<OrderSeatList> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(OrderSeatList::getListState,OrderConstant.listState.WAIT_ARRIVE)
                    .eq(OrderSeatList::getListState, OrderConstant.listState.IN_USE)
                    .eq(OrderSeatList::getOrderType,OrderConstant.OrderType.LONG_RENT);
            orderSeatListMapper.update(null,wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }


}