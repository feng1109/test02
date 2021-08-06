package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 会议室预约签到
 * @Author: 王鹏滔
 * @Date: 2021/6/10 9:24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ArriveMeetingOrderReqVO")
@ApiModel(value="会议室预约请求签到打卡", description="会议室预约请求签到打卡")
public class ArriveMeetingOrderReqVO implements Serializable {

    private static final long serialVersionUID = -8761920333973956736L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String orderMeetingId;

    @ApiModelProperty(value = "打卡方式（1.距离打卡 2.扫码打卡）",required = true)
    @NotNull
    private Integer clockType;

    @ApiModelProperty(value = "订单类型",required = true)
    @NotNull
    private Integer orderType;

    @ApiModelProperty(value = "签到距离")
    private Integer distance;








}