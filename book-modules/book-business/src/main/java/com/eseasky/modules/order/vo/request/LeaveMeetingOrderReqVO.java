package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * LeaveMeetingOrderReqVO
 * @Author: 王鹏滔
 * @Date: 2021/6/10 15:03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "LeaveMeetingOrderReqVO")
@ApiModel(value="会议室预约请求签退", description="会议室预约请求签退")
public class LeaveMeetingOrderReqVO implements Serializable {

    private static final long serialVersionUID = 3895914065348378628L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String id;

    @ApiModelProperty(value = "打卡方式（1.距离打卡 2.扫码打卡）",required = true)
    @NotNull
    private Integer clockType;

    @ApiModelProperty(value = "订单类型",required = true)
    @NotNull
    private Integer orderType;

    @ApiModelProperty(value = "签到距离")
    private Integer distance;

}