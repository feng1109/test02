package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: ListComm entReqVO
 * @Author lc
 * @Date: 2021/4/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ListCommentReqVO")
@ApiModel(value="订单评价", description="订单评价")
public class ListCommentReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull(message = "请传入订单id")
    private String orderListId;

    @ApiModelProperty(value = "订单类型",required = true)
    @NotNull(message = "请传入订单类型")
    private Integer orderType;


    @ApiModelProperty(value = "房间id",required = true)
    @NotNull(message = "请传入房间id")
    private String roomId;

    @ApiModelProperty(value = "等级",required = true)
    @NotNull(message = "请传入干净等级")
    private Integer level;



}