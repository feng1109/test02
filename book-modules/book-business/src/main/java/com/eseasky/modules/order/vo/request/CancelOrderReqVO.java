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
@Alias(value = "CancelOrderReqVO")
@ApiModel(value="请求取消订单", description="请求取消订单")
public class CancelOrderReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String orderSeatId;


    @ApiModelProperty(value = "订单类型",required = true)
    @NotNull
    private Integer orderType;

}
