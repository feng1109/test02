package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: ArriveOrdeReqVO
 * @Author lc
 * @Date: 2021/4/27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "AwayOrderReqVO")
@ApiModel(value="请求暂离（返回）", description="请求暂离（返回）")
public class AwayOrderReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String orderSeatId;

    @ApiModelProperty(value = "打卡方式",required = true)
    @NotNull
    private Integer clockType;

    @ApiModelProperty(value = "签到距离")
    private Integer distance;








}