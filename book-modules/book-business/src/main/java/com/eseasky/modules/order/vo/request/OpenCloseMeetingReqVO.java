package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @describe:
 * @title: ShowClockReq
 * @Author lc
 * @Date: 2021/4/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "OpenAppoinemenetReqVO")
@ApiModel(value="开启或关闭预约", description="开启或关闭预约")
public class OpenCloseMeetingReqVO implements Serializable {

    private static final long serialVersionUID = 2734623089619154436L;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "状态（开启还是关闭预约），开启：1，关闭：2")
    private Integer state;


}