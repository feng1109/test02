package com.eseasky.modules.order.controller;


import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.service.OrderApproveService;
import com.eseasky.modules.order.vo.request.ApproveAgreeReqVO;
import com.eseasky.modules.order.vo.request.ApproveRejectReqVO;
import com.eseasky.modules.order.vo.request.ShowApproveListReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 审批 前端控制器
 * </p>
 *
 * @author
 * @since 2021-06-11
 */
@Api(value = "预约审批" ,tags = "预约审批")
@RestController
@RequestMapping("/order/orderApprove")
public class OrderApproveController {

    @Autowired
    OrderApproveService orderApproveService;

    @ApiOperation(value = "查看审批列表(包括详情数据)")
    @PostMapping("showApproveList")
    public R showApproveList(@RequestBody ShowApproveListReqVO showApproveListReqVO){
        return orderApproveService.showApproveList(showApproveListReqVO);
    }

    @ApiOperation(value = "同意")
    @PostMapping("agreeApproveList")
//    @PreAuthorize("hasAuthority('order:approve:agreeApproveList')")
    public R agreeApproveList(@RequestBody ApproveAgreeReqVO approveAgreeReqVO){
        return orderApproveService.agreeApproveList(approveAgreeReqVO);
    }

    @ApiOperation(value = "反对")
    @PostMapping("rejectApproveList")
//    @PreAuthorize("hasAuthority('order:approve:rejectApproveList')")
    public R rejectApproveList(@RequestBody ApproveRejectReqVO approveRejectReqVO){
        return orderApproveService.rejectApproveList(approveRejectReqVO);
    }

}

