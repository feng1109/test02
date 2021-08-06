package com.eseasky.modules.order.controller;


import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.dto.SendNoticeDTO;
import com.eseasky.modules.order.entity.OrderNotice;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.vo.request.OrderNoticeReqVO;
import com.eseasky.modules.order.vo.response.ListWithPage;
import com.eseasky.modules.order.vo.response.OrderNoticeRepVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2021-05-14
 */
@Api(value = "预约通知", tags = "预约通知")
@RestController
@RequestMapping("/orderNotice")
public class OrderNoticeController {

    @Autowired
     OrderNoticeService orderNoticeService;

    @ApiOperation(value = "推送通知")
    @PostMapping(value = "/sendNotice")
    public void sendNotice(SendNoticeDTO sendNoticeDTO){
        orderNoticeService.sendNotice(sendNoticeDTO);
    }

    @ApiOperation(value = "移动端查看通知列表")
    @PostMapping(value = "/showNoticeList")
    public R<ListWithPage<OrderNoticeRepVO>> showNoticeList(@RequestBody OrderNoticeReqVO orderNoticeReqVO){
       return   orderNoticeService.showNoticeList(orderNoticeReqVO);
    }

    @ApiOperation(value = "移动端：用户删除通知")
    @DeleteMapping(value = "/delNotice")
    public R delNotice(String orderNoticeId){
        return orderNoticeService.delNotice(orderNoticeId);
    }

    @ApiOperation(value = "移动端：读取通知详细信息")
    @DeleteMapping(value = "/readNotice")
    public R readNotice(String orderNoticeId){
        return orderNoticeService.readNotice(orderNoticeId);
    }

    @ApiOperation(value = "移动端：获取通知条数(0:未读,1:已读)")
    @GetMapping(value = "/getNoticeCount")
    public R<Integer> getNoticeCount(Integer isRead){
        return orderNoticeService.getNoticeCount(isRead);
    }
}

