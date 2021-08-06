package com.eseasky.modules.order.controller;


import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.dto.OrderListInfoDTO;
import com.eseasky.modules.order.dto.SpaceInfoDTO;
import com.eseasky.modules.order.service.OrderSeatListService;
import com.eseasky.modules.order.vo.OrderSeatListVO;
import com.eseasky.modules.order.vo.request.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2021-04-12
 */
@Api(value = "预约订单", tags = "预约订单")
@RestController
@RequestMapping("/order/orderSeatList")
public class OrderSeatListController {

    @Autowired
    OrderSeatListService orderSeatListService;

    @ApiOperation(value = "测试")
    @GetMapping("/test")
    public R test(){
        return orderSeatListService.test();
    }

    @ApiOperation(value = "查看空间被占座位及状态(测试)")
    @PostMapping(value = "/getUsedSeat")
    public R getUsedSeat(@RequestBody OrderListInfoDTO orderListInfoDTO){
        R<List<Map<String, Object>>> ok = R.ok(orderSeatListService.getUsedSeat(orderListInfoDTO));
        return ok;
    }

    @ApiOperation(value = "根据id查看空间信息(测试)")
    @PostMapping(value = "/getAreaInfo")
    public R getAreaInfo(@RequestBody SpaceInfoDTO spaceInfoDTO){
        return R.ok(orderSeatListService.getAreaInfo(spaceInfoDTO));
    }

    @ApiOperation(value = "pc端数据统计")
    @Log(value="pc端数据统计",type = 1)
    @PostMapping(value = "/showPCStatisticsData")
    public R showPCStatisticsData(@RequestBody @Valid StatisticsDataReqVO statisticsDataReqVO){
        return orderSeatListService.showPCStatisticsData(statisticsDataReqVO);
    }

    @ApiOperation(value = "移动端：查看常用地点")
    @PostMapping(value = "/showOftenUseArea")
    @Log(value="移动端：查看常用地点",type = 0)
    public R showOftenUseArea(@RequestBody ShowOftenUseAreaReqVO showOftenUseAreaReqVO){
        return orderSeatListService.showOftenUseArea(showOftenUseAreaReqVO);
    }

    @ApiOperation(value = "移动端：查看最近一次预约(短租与长租)")
    @GetMapping(value = "/showLastTimeList")
    @Log(value="移动端：查看最近一次预约(短租与长租)",type = 0)
    public R showLastTimeList( String userId){
        return orderSeatListService.showLastTimeList(userId);
    }

    @ApiOperation(value = "移动端：查看订单详情")
    @GetMapping(value = "/showListDetail")
    @Log(value="移动端：查看订单详情",type = 0)
        public R showListDetail(String orderSeatId){
        return orderSeatListService.showListDetail(orderSeatId);
    }

    @ApiOperation(value = "移动端：查看用户短租订单")
    @PostMapping(value = "/showShortRentList")
    @Log(value="移动端：查看用户短租订单",type = 0)
    public R showShortRentList(@RequestBody @Valid UserListReqVO userListReqVO){
        return orderSeatListService.userShortRentList(userListReqVO);
    }

    @ApiOperation(value = "移动端：查看用户长租订单")
    @PostMapping(value = "/showLongRentList")
    @Log(value="移动端：查看用户长租订单",type = 0)
    public R showLongRentList(@RequestBody @Valid UserListReqVO userListReqVO){
        return orderSeatListService.userLongRentList(userListReqVO);
    }

    @ApiOperation(value = "移动端：长租明细")
    @PostMapping(value = "/showLongRentDetail")
    @Log(value="移动端：长租签到记录",type = 0)
    public R showLongRentDetail(@RequestBody @Valid LongDetailReqVO longDetailReqVO){
        return orderSeatListService.showLongRentDetail(longDetailReqVO);
    }

    @ApiOperation(value = "移动端：查看用户信息（我的）")
    @Log(value="移动端：查看用户信息（我的）",type = 0)
    @GetMapping(value = "/showUserInfo")
    public R showUserInfo(){
        return orderSeatListService.showUserInfo();
    }

    @ApiOperation(value = "移动端：预约统计")
    @Log(value="移动端：预约统计",type = 0)
    @GetMapping(value = "/showStatistics")
    public R showStatistics(String month){
        return orderSeatListService.showStatistics(month);
    }

    @ApiOperation(value = "移动端：查看打卡界面")
    @Log(value="移动端：查看打卡界面",type = 0)
    @PostMapping(value = "/showClockInfo")
    public R showClockInfo(@RequestBody  ShowClockReqVO showClockReqVO){
        return orderSeatListService.showClockInfo(showClockReqVO);
    }

    @ApiOperation(value = "移动端：预约")
    @Log(value="移动端：预约",type = 0)
    @PostMapping(value = "/creatOrderList")
    public R creatOrderList(@RequestBody OrderSeatListVO orderSeatListVO){
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        return orderSeatListService.creatOrderList(orderSeatListVO,sysUserDTO);
    }

    @ApiOperation(value = "移动端：取消预约")
    @Log(value="移动端：取消预约",type = 0)
    @PostMapping(value = "/cancelOrderList")
    public R cancelOrderList(@RequestBody @Valid CancelOrderReqVO cancelOrderReqVO){
        return orderSeatListService.cancelOrderList(cancelOrderReqVO);
    }

    @ApiOperation(value = "移动端：签到")
    @Log(value="移动端：签到",type = 0)
    @PostMapping(value = "/arriveOrderList")
    public R arriveOrderList(@RequestBody @Valid ArriveOrderReqVO arriveOrderReqVO){
        return orderSeatListService.arriveOrderList(arriveOrderReqVO);
    }

    @ApiOperation(value = "移动端：暂离（短租）")
    @Log(value="移动端：暂离（短租）",type = 0)
    @PostMapping(value = "/awayOrderList")
    public R awayOrderList(@RequestBody @Valid AwayOrderReqVO awayOrderReqVO){
        return orderSeatListService.awayOrderList(awayOrderReqVO);
    }

    @ApiOperation(value = "移动端：暂离返回（短租）")
    @Log(value="移动端：暂离返回（短租）",type = 0)
    @PostMapping(value = "/backOrderList")
    public R backOrderList(@RequestBody @Valid AwayOrderReqVO awayOrderReqVO){
        return orderSeatListService.backOrderList(awayOrderReqVO);
    }

    @ApiOperation(value = "移动端：续约（短租）")
    @Log(value="移动端：续约（短租",type = 0)
    @PostMapping(value = "/continueOrderList")
    public R continueOrderList(@RequestBody @Valid ContinueOrderReqVO continueOrderReqVO){
        return orderSeatListService.continueOrderList(continueOrderReqVO);
    }

    @ApiOperation(value = "移动端：签退")
    @Log(value="移动端：签退",type = 0)
    @PostMapping(value = "/leaveOrderList")
    public R leaveOrderList(@RequestBody @Valid  LeaveOrderReqVO leaveOrderReqVO){
        return orderSeatListService.leaveOrderList(leaveOrderReqVO);
    }

}

