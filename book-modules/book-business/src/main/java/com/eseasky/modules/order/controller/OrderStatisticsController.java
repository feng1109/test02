package com.eseasky.modules.order.controller;

import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.service.OrderStatisticsService;
import com.eseasky.modules.order.vo.request.AnaRoomReqVO;
import com.eseasky.modules.order.vo.request.OrderListReqVO;
import com.eseasky.modules.order.vo.request.StaSpcReqVO;
import com.eseasky.modules.order.vo.request.StaUserReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @describe:
 * @title: OrderStatisticsController
 * @Author lc
 * @Date: 2021/5/20
 */
@Api(value = "统计中心", tags = "统计中心")
@RestController
@RequestMapping("/order/orderStatistics")
public class OrderStatisticsController {

    @Autowired
    OrderStatisticsService orderStatisticsService;

    @ApiOperation(value = "空间统计信息")
    @PostMapping("/showSpaceStatistics")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getSpaceStatistics')")
    public R showSpaceStatistics(@RequestBody @Valid StaSpcReqVO staSpcReqVO){
       return orderStatisticsService.showSpaceStatistics(staSpcReqVO);
    }

    @ApiOperation(value = "空间分析信息")
    @PostMapping("/showSpaceAnalysis")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getSpaceAnalysis')")
    public R showSpaceAnalysis(@RequestBody @Valid AnaRoomReqVO anaRoomReqVO){
        return orderStatisticsService.showSpaceAnalysis(anaRoomReqVO);
    }

    @ApiOperation(value = "人员统计信息")
    @PostMapping("/showUserStatistics")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getUserStatistics')")
    public R showUserStatistics(@RequestBody @Valid StaUserReqVO staUserReqVO){
        return orderStatisticsService.showUserStatistics(staUserReqVO);
    }

    @ApiOperation(value = "人员分析信息")
    @GetMapping("/showUserAnalysis")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getUserAnalysis')")
    public R showUserStatistics( String userId){
        return orderStatisticsService.showUserAnalysis(userId);
    }

    @ApiOperation(value = "预约列表查询")
    @PostMapping(value = "/showOrderList")
    @Log(value="查看建筑预约订单",type = 0)
//    @PreAuthorize("hasAuthority('order:orderStatistics:getOrderList')")
    public R showOrderList(@RequestBody @Valid OrderListReqVO orderListReqVO){
        return orderStatisticsService.showOrderList(orderListReqVO);
    }


    @ApiOperation(value = "查看短租预约详情")
    @GetMapping(value = "/showShortDetail")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getShortDetail')")
    public R showShortDetail(String orderSeatId){
        return orderStatisticsService.showShortDetail(orderSeatId);
    }

    @ApiOperation(value = "查看长租预约详情")
    @GetMapping(value = "/showLongDetail")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getLongDetail')")
    public R showLongDetail(String orderSeatId){
        return orderStatisticsService.showLongDetail(orderSeatId);
    }

    @ApiOperation(value = "查看拼团预约详情")
    @GetMapping(value = "/showGroupDetail")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getLongDetail')")
    public R showGroupDetail(String orderGroupId){
        return orderStatisticsService.showGroupDetail(orderGroupId);
    }

    @ApiOperation(value = "导出预约列表")
    @PostMapping(value = "/outOrderList")
//    @PreAuthorize("hasAuthority('order:orderStatistics:outOrderList')")
    public void outOrderList(@RequestBody @Valid OrderListReqVO orderListReqVO, HttpServletResponse httpServletResponse){
        orderStatisticsService.outOrderList(orderListReqVO,httpServletResponse);
    }

    @ApiOperation(value = "预约列表综合楼和房间下拉框")
    @GetMapping("/getBuildAndRoomDropDown")
//    @PreAuthorize("hasAuthority('order:orderStatistics:getBuildAndRoomDropDown')")
    public R getBuildAndRoomDropDown() {
        return orderStatisticsService.getBuildAndRoomDropDown();
    }
}