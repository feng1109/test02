package com.eseasky.modules.order.controller;


import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.service.OrderBlacklistRuleService;
import com.eseasky.modules.order.vo.request.CreateBlrReqVO;
import com.eseasky.modules.order.vo.request.EditBlrReqVO;
import com.eseasky.modules.order.vo.request.ShowBlrListReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
@Api(value = "黑名单规则",tags = "黑名单规则")
@RestController
@RequestMapping("/order/orderBlacklistRule")
public class OrderBlacklistRuleController {

    @Autowired
    OrderBlacklistRuleService orderBlacklistRuleService;


    @ApiOperation(value = "创建黑名单规则")
    @PostMapping(value = "/creatBlacklistRule")
    public R creatBlacklistRule(@RequestBody @Valid CreateBlrReqVO createBlrReqVO){
        return orderBlacklistRuleService.creatBlacklistRule(createBlrReqVO);
    }

    @ApiOperation(value = "获取已配置黑名单组织id")
    @GetMapping(value = "/getAlreadyCreateOrgId")
    public R getAlreadyCreateOrgId(){
        return orderBlacklistRuleService.getAlreadyCreateOrgId();
    }

    @ApiOperation(value = "查看黑名单详情")
    @GetMapping(value = "/showBlrDetail")
    public R showBlrDetail(String ruleId){
        return  orderBlacklistRuleService.showBlrDetail(ruleId);
    }

    @ApiOperation(value = "编辑黑名单规则")
    @PostMapping(value = "/editBlacklistRule")
    public R editBlacklistRule(@RequestBody @Valid EditBlrReqVO editBlrReqVO){
        return orderBlacklistRuleService.editBlacklistRule(editBlrReqVO);
    }

    @ApiOperation(value = "查看黑名单规则列表")
    @PostMapping(value = "/showBlrList")
    public R showBlrList(@RequestBody @Valid ShowBlrListReqVO showBlrListReqVO){
        return orderBlacklistRuleService.showBlrList(showBlrListReqVO);
    }

    @ApiOperation(value = "批量删除")
    @DeleteMapping(value = "/delBlacklistRule")
    public R delBlacklistRule(String[] ruleIds){
        return orderBlacklistRuleService.delBlacklistRule(ruleIds);
    }



}

