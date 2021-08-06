package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * MeetingContinueOrderReqVO
 * @Author: 王鹏滔
 * @Date: 2021/6/9 15:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "MeetingContinueOrderReqVO")
@ApiModel(value="请求续约订单", description="请求续约订单")
public class MeetingContinueOrderReqVO {

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String id;

    @ApiModelProperty(value = "打卡方式",required = true)
    @NotNull
    private Integer clockType;

    @ApiModelProperty(value = "签到距离")
    private Integer distance;

    @ApiModelProperty(value = "续约开始时间",required = true)
    @NotNull
    private Date orderStartTime;

    @ApiModelProperty(value = "续约结束时间",required = true)
    @NotNull
    private Date orderEndTime;

    @ApiModelProperty(value = "房间id",required = true)
    @NotNull
    private String roomId;
}