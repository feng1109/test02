package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @describe:
 * @title: InsertApproveInfoDTO
 * @Author lc
 * @Date: 2021/6/24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "插入审批数据")
public class InsertApproveInfoDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "订单id")
    private String orderListId;

    @ApiModelProperty(name = "订单类型")
    private Integer orderType;

    @ApiModelProperty(name = "申请人id")
    private String userId;

    @ApiModelProperty(name = "申请人姓名")
    private String userName;

    @ApiModelProperty(name = "申请时间")
    private Date applyTime;

    @ApiModelProperty(name = "申请原因")
    private String reason;

    @ApiModelProperty(value = "预约开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private Date orderEndTime;

    @ApiModelProperty(name = "地点")
    private String area;

    @ApiModelProperty(name = "审批人")
    private String[] approvers;

}