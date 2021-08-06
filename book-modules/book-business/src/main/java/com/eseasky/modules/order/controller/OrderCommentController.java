package com.eseasky.modules.order.controller;

import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.service.OrderCommentService;
import com.eseasky.modules.order.vo.request.ListCommentReqVO;
import com.eseasky.modules.order.vo.request.RoomCommentReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @describe:
 * @title: OrderCommentController
 * @Author lc
 * @Date: 2021/5/10
 */
@Api(value = "预约评价",tags = "预约评价")
@RestController
@RequestMapping("order/orderComment")
public class OrderCommentController {

    @Autowired
    OrderCommentService orderCommentService;

    @ApiOperation(value = "移动端：订单评价")
    @PostMapping(value = "/commentList")
    @Log(value="订单评价",type = 1)
    public R showLongRentDetail(@RequestBody @Valid ListCommentReqVO listCommentReqVO){
        return orderCommentService.commentList(listCommentReqVO);
    }

    @ApiOperation(value = "pc端：查看空间订单评价")
    @PostMapping(value = "/showListComment")
    @Log(value="查看订单评价",type = 2)
    public R showListComment(@RequestBody @Valid RoomCommentReqVO roomCommentReqVO){
        return orderCommentService.showListComment(roomCommentReqVO);
    }

    @ApiOperation(value = "pc端：删除评价")
    @DeleteMapping(value = "/delListComment")
    @Log(value="删除评价",type = 2)
    public R delListComment(String orderCommentId){
        return orderCommentService.delListComment(orderCommentId);
    }

}