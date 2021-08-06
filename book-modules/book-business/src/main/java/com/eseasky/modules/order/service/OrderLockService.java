package com.eseasky.modules.order.service;

import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.vo.request.GroupJoinReqVO;

import java.util.Date;

/**
 * @describe:
 * @title: OrderSeatLockService
 * @Author lc
 * @Date: 2021/5/12
 */

public interface OrderLockService {

    R lockCreatList(OrderSeatList orderSeatList, String tenantCode);

    R lockCreatGroupList(OrderGroupList orderGroupList, String tenantCode);

    R shortContinue(String tenantCode, String orderSeatId, Date orderEndTime, String seatId);

    R joinGroup(GroupJoinReqVO groupJoinReqVO, SysUserDTO sysUserDTO);

    R approveAgree(String approveId);

    R approveReject(String approveId);
}