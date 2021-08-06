package com.eseasky.modules.order.service;

import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.modules.order.vo.request.*;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-06-08
 */
public interface OrderGroupListService extends IService<OrderGroupList> {

    R createOrder(GroupCreateReqVO groupCreateReqVO);

    R joinOrder(GroupJoinReqVO groupJoinReqVO);

    R arriveOrder(GroupArriveReqVO groupArriveReqVO);

    R leaveOrder(GroupLeaveReqVO groupLeaveReqVO);

    R inviteGroup(String orderGroupId);

    R showGroupDetail(String orderGroupDetailId);

    R showGroupRentList(UserListReqVO userListReqVO);
}
