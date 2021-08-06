package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @describe:
 * @title: ShowClockReq
 * @Author lc
 * @Date: 2021/4/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ShowClockReqVO")
@ApiModel(value="查看打卡界面", description="查看打卡界面")
public class ShowClockReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "用户id" )
    private String userId;

    @ApiModelProperty(value = "打卡方式")
    private Integer clockType;

    @ApiModelProperty(value = "查询条件：订单id/人名")
    private String queryCondition;


}