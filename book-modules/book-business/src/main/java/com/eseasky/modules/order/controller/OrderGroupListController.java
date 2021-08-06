package com.eseasky.modules.order.controller;


import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.service.OrderGroupListService;
import com.eseasky.modules.order.vo.request.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Api(value = "拼团预约", tags = "拼团预约")
@RestController
@RequestMapping("/order/orderGroupList")
public class OrderGroupListController {

    @Autowired
    OrderGroupListService orderGroupListService;

    @ApiOperation(value = "发起拼团预约")
    @PostMapping(value = "/createOrder")
    public R creatOrder(@RequestBody @Valid GroupCreateReqVO groupCreateReqVO) {
        return orderGroupListService.createOrder(groupCreateReqVO);
    }

    @ApiOperation(value = "加入拼团预约")
    @PostMapping(value = "/joinOrder")
    public R joinOrder(@RequestBody @Valid GroupJoinReqVO groupJoinReqVO) {
        return orderGroupListService.joinOrder(groupJoinReqVO);
    }

//    @ApiOperation(value = "取消预约（参团人为退出预约）")
//    @PostMapping(value = "/cancelOrder")
//    public R cancelOrder() {
//        return orderGroupListService.cancelOrder();
//    }

    @ApiOperation(value = "签到")
    @PostMapping(value = "/arriveOrder")
    public R arriveOrder(@RequestBody @Valid GroupArriveReqVO groupArriveReqVO ){
        return orderGroupListService.arriveOrder(groupArriveReqVO);
    }

    @ApiOperation(value = "签退")
    @PostMapping(value = "/leaveOrder")
    public R leaveOrder(@RequestBody @Valid GroupLeaveReqVO groupLeaveReqVO){
        return orderGroupListService.leaveOrder(groupLeaveReqVO);
    }

    @ApiOperation(value = "邀请拼团")
    @GetMapping(value = "/inviteGroup")
    public R inviteGroup(String orderGroupId){
        return orderGroupListService.inviteGroup(orderGroupId);
    }


    @ApiOperation(value = "订单详情")
    @GetMapping(value = "/showGroupDetail")
    public R showGroupDetail(String orderGroupDetailId){
        return orderGroupListService.showGroupDetail(orderGroupDetailId);
    }

    @ApiOperation(value = "查看订单列表")
    @PostMapping(value = "/showGroupRentList")
    public R showGroupRentList(@RequestBody UserListReqVO userListReqVO){
        return orderGroupListService.showGroupRentList(userListReqVO);
    }
}

