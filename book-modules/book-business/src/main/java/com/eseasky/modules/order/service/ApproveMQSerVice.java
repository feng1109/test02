package com.eseasky.modules.order.service;


import java.util.Map;

/**
 * @describe:
 * @title: ApproveMQSerVice
 * @Author lc
 * @Date: 2021/7/4
 */

public interface ApproveMQSerVice {

    void handleApproveOff(Map<String,Object> msg);



}