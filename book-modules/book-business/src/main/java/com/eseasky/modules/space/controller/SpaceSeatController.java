package com.eseasky.modules.space.controller;


import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceCanvasVO;
import com.eseasky.modules.space.vo.request.ConfirmOrderParam;
import com.eseasky.modules.space.vo.request.QueryMobileSeatParam;
import com.eseasky.modules.space.vo.request.QuickOrderParam;
import com.eseasky.modules.space.vo.request.SaveSeatVO;
import com.eseasky.modules.space.vo.response.ConfirmOrderVO;
import com.eseasky.modules.space.vo.response.QueryMobileSeatListVO;
import com.eseasky.modules.space.vo.response.SeatInfoForQuickOrder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>
 * 座位服务
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Api(value = "座位服务", tags = "座位服务")
@RestController
@RequestMapping("/space/spaceSeat")
public class SpaceSeatController {

    @Autowired
    private SpaceSeatService seatService;

    @Log(value = "座位管理：新增|修改座位（全量）", type = 0)
    @ApiOperation(value = "座位管理：新增|修改座位（全量）", notes = "座位管理：新增|修改座位（全量）")
    @PostMapping(value = "addSeat", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceSeat:addSeat')")
    public R<String> addSeat(@RequestBody @Valid SpaceCanvasVO canvasVO) {
        return seatService.addOrUpdateSeat(canvasVO);
    }

    @Log(value = "座位管理：查询座位布局列表", type = 0)
    @ApiOperation(value = "座位管理：查询座位布局列表", notes = "座位管理：查询座位布局列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "roomId", value = "空间id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "getSpaceSeatList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceSeat:getSpaceSeatList')")
    public R<List<SaveSeatVO>> getSpaceSeatList(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return seatService.getSpaceSeatList(param);
    }

    /***************** 统计中心接口 *****************/

    @Log(value = "统计中心：座位展示列表", type = 0)
    @ApiOperation(value = "统计中心：座位展示列表", notes = "统计中心：座位展示列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "roomId", value = "空间id", dataTypeClass = String.class, required = true)})
    @PostMapping(value = "getStatisticSeatList", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('space:spaceSeat:getStatisticSeatList')")
    public R<QueryMobileSeatListVO<SaveSeatVO>> getStatisticSeatList(@RequestBody @ApiParam(hidden = true) JSONObject param) {
        return seatService.getStatisticSeatList(param);
    }

    /***************** 以下是手机端接口 *****************/

    // @Log(value = "手机预约界面：座位选择列表", type = 1)
    @ApiOperation(value = "手机预约界面：座位选择列表", notes = "手机预约界面：座位选择列表")
    @PostMapping(value = "getMobileSeatList", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<QueryMobileSeatListVO<SaveSeatVO>> getMobileSeatList(@RequestBody @Valid QueryMobileSeatParam param) {
        return seatService.getMobileSeatList(param);
    }

    // @Log(value = "手机预约界面：快速预约", type = 1)
    @ApiOperation(value = "手机预约界面：快速预约", notes = "手机预约界面：快速预约")
    @PostMapping(value = "mobileQuickOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<SeatInfoForQuickOrder> mobileQuickOrder(@RequestBody @Valid QuickOrderParam param) {
        return seatService.mobileQuickOrder(param);
    }


    // @Log(value = "手机预约界面：确认订单时查询座位|座位组|房间详情", type = 1)
    @ApiOperation(value = "手机预约界面：确认订单时查询座位|座位组|房间详情", notes = "手机预约界面：确认订单时查询座位|座位组|房间详情")
    @PostMapping(value = "getInfoToConfirmOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<ConfirmOrderVO> getInfoToConfirmOrder(@RequestBody @Valid ConfirmOrderParam param) {
        return seatService.getInfoToConfirmOrder(param);
    }

}

