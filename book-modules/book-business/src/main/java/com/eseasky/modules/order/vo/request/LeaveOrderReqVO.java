package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: leaveOrderReqVO
 * @Author lc
 * @Date: 2021/5/6
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "leaveOrderReqVO")
@ApiModel(value="请求签退", description="请求签退")
public class LeaveOrderReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String orderSeatId;

    @ApiModelProperty(value = "打卡方式（1.距离打卡 2.扫码打卡）",required = true)
    @NotNull
    private Integer clockType;

    @ApiModelProperty(value = "订单类型",required = true)
    @NotNull
    private Integer orderType;

    @ApiModelProperty(value = "签到距离")
    private Integer distance;

}