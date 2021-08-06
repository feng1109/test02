package com.eseasky.modules.order.controller;


import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.service.OrderFeedBackService;
import com.eseasky.modules.order.vo.request.OrderFeedBackAddReqVO;
import com.eseasky.modules.order.vo.request.OrderFeedBackHandReqVO;
import com.eseasky.modules.order.vo.request.OrderFeedBackPageReqVO;
import com.eseasky.modules.order.vo.response.FeedBackRepVO;
import com.eseasky.modules.order.vo.response.ListWithPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户反馈
 *
 * @author wpt
 * @since 2021-07-16
 */
@RestController
@RequestMapping("/order/orderFeedBack")
@Api(value = "订单反馈",tags = "订单反馈")
public class OrderFeedBackController {

    @Autowired
    private OrderFeedBackService orderFeedBackService;

    @ApiOperation(value = "移动端：订单反馈")
    @PostMapping(value = "/addOrderFreeBack")
    @Log(value="移动端：订单反馈",type = 1)
    public R<String> addOrderFreeBack(@RequestBody @Validated OrderFeedBackAddReqVO orderFeedBackAddReqVO){
        return orderFeedBackService.addOrderFreeBack(orderFeedBackAddReqVO);
    }

    @ApiOperation(value = "pc端：查看空间订单反馈")
    @PostMapping(value = "/showFeedBackPage")
    @Log(value="pc端：查看空间订单反馈",type = 2)
    public R<ListWithPage<FeedBackRepVO>> showFeedBackPage(@RequestBody OrderFeedBackPageReqVO orderFeedBackPageReqVO){
        return orderFeedBackService.showFeedBackPage(orderFeedBackPageReqVO);
    }

    @ApiOperation(value = "pc端：批量处理反馈")
    @PostMapping(value = "/batchHandleFeedBack")
    @Log(value="pc端：批量处理反馈",type = 2)
    public R<String> batchHandleFeedBack(@RequestBody OrderFeedBackHandReqVO orderFeedBackHandReqVO){
        return orderFeedBackService.batchHandleFeedBack(orderFeedBackHandReqVO);
    }

    @ApiOperation(value = "pc端：获取场馆下拉框")
    @PostMapping(value = "/getBuildNameDropDownBox")
    @Log(value="pc端：获取场馆下拉框",type = 2)
    public R<List<JSONObject>> getBuildNameDropDownBox(){
        return orderFeedBackService.getBuildNameDropDownBox();
    }

}

