package com.eseasky.modules.order.controller;


import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.dto.SysUserOrgTreeDTO;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.service.OrderMeetingService;
import com.eseasky.modules.order.vo.OrderMeetingVO;
import com.eseasky.modules.order.vo.request.*;
import com.eseasky.modules.order.vo.response.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 会议室预约订单 前端控制器
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Api(value = "会议室预约" ,tags = "会议室预约")
@RestController
@RequestMapping("/order/orderMeeting")
public class OrderMeetingController {

    @Autowired
    private OrderMeetingService orderMeetingService;

    @ApiOperation(value = "移动端：会议室预约创建订单")
    @PostMapping("/createOrder")
    @Log(value="移动端：创建订单",type = 1)
    public R<OrderMeetingList> createOrder(@RequestBody OrderMeetingVO orderMeetingVO) {
        return orderMeetingService.createOrder(orderMeetingVO);
    }

    @ApiOperation(value = "移动端：取消会议室订单")
    @Log(value="移动端：取消会议室预约",type = 1)
    @PostMapping(value = "/cancelOrder")
    public R<String> cancelOrderList(@RequestBody @Valid CancelMeetingOrderReqVO cancelMeetingOrderReqVO){
        return orderMeetingService.cancelOrder(cancelMeetingOrderReqVO);
    }

    @ApiOperation(value = "移动端：会议室预约签到")
    @Log(value="移动端：会议室预约签到",type = 1)
    @PostMapping(value = "/arriveOrder")
    public R<String> arriveOrder(@RequestBody @Valid ArriveMeetingOrderReqVO arriveMeetingOrderReqVO){
        return orderMeetingService.arriveOrder(arriveMeetingOrderReqVO);
    }

    @ApiOperation(value = "移动端：分页获取会议室订单")
    @PostMapping("/getMeetingOrder")
    @Log(value="移动端：分页获取会议室订单",type = 1)
    public R<ListWithPage<UserMeetingListRepVO>> getMeetingOrderPage(@RequestBody @Valid UserMeetingOrderReqVO userMeetingOrderReqVO){
        return orderMeetingService.getMeetingOrderPage(userMeetingOrderReqVO);
    }

    @ApiOperation(value = "移动端：获取会议室记录")
    @PostMapping("/getDataList")
    @Log(value="移动端：获取会议室记录",type = 1)
    public R<ListWithPage<UserMeetingInfoRepVO>> getMeetingRecord(@RequestBody @Valid UserMeetingListReqVO userMeetingListReqVO){
        return orderMeetingService.getMeetingRecord(userMeetingListReqVO);
    }

    @ApiOperation(value = "移动端：会议室打卡明细")
    @PostMapping(value = "/showMeetingRentDetail")
    @Log(value="移动端：会议室长租签到明细",type = 1)
    public R<ListWithPage<OrderMeetingRentDetailRepVO>> showMeetingRentDetail(@RequestBody @Valid MeetingClockDetailReqVO meetingClockDetailReqVO){
        return orderMeetingService.showMeetingRentDetail(meetingClockDetailReqVO);
    }

    @ApiOperation(value = "移动端：查看会议室预约订单详情")
    @PostMapping(value = "/getMeetingDetail")
    @Log(value="移动端：查看订单详情",type = 1)
    public R<OrderMeetingListDetailRepVO> getMeetingDetail(@RequestBody MeetingDetailReqVO meetingDetailReqVO){
        return orderMeetingService.getMeetingDetail(meetingDetailReqVO);
    }

    @ApiOperation(value = "移动端：查看会议室预约打卡页面")
    @PostMapping(value = "/showClockInfo")
    @Log(value="移动端：查看会议室预约打卡页面",type = 1)
    public R<List<MeetingClockRepVO>> showClockInfo(@RequestBody MeetingClockReqVO meetingClockReqVO){
        return orderMeetingService.showClockInfo(meetingClockReqVO);
    }

    @ApiOperation(value = "移动端：开启或关闭会议")
    @PostMapping(value = "/openOrCloseMeeting")
    @Log(value="移动端：开启或关闭会议",type = 1)
    public R<String> openOrCloseMeeting(@RequestBody OpenCloseMeetingReqVO openCloseMeetingReqVO){
        return orderMeetingService.openOrCloseMeeting(openCloseMeetingReqVO);
    }

    @ApiOperation(value = "移动端：获取组织及用户树形结构数据", notes = "移动端：获取组织及用户树形结构数据")
    @PostMapping("getTreeUsers")
    @Log(value = "移动端：获取组织及用户树形结构数据",type = 1)
    public R<List<SysUserOrgTreeDTO>> getTreeUsers() {
        return orderMeetingService.getTreeUsers();
    }

    @ApiOperation(value = "移动端：获取子组织及用户树形结构数据", notes = "移动端：获取子组织及用户树形结构数据")
    @PostMapping("getTreeUsersByIsOrgAndId")
    @Log(value = "移动端：获取子组织及用户树形结构数据",type = 1)
    public R<List<SysUserOrgTreeDTO>> getTreeUsersByIsOrgAndId(@RequestBody SysUserOrgTreeReqVO sysUserOrgTreeReqVO) {
        return orderMeetingService.getTreeUsersByIsOrgAndId(sysUserOrgTreeReqVO);
    }

    @ApiOperation(value = "PC端：单次预约详情", notes = "PC端：单次预约详情")
    @GetMapping("getShortDetail")
    @Log(value = "PC端：单次预约详情",type = 0)
    public R<OrderMeetingShortDetailRepVO> getShortDetail(String orderMeetingId) {
        return orderMeetingService.getShortDetail(orderMeetingId);
    }

    @ApiOperation(value = "PC端：长租详情", notes = "PC端：长租详情")
    @GetMapping("getLongDetail")
    @Log(value = "PC端：长租详情",type = 0)
    public R<OrderMeetingLongDetailRepVO> getLongDetail(String orderMeetingId) {
        return orderMeetingService.getLongDetail(orderMeetingId);
    }
}

