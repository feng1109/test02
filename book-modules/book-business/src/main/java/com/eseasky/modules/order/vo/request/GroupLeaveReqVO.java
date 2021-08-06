package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: GroupArriveReqVO
 * @Author lc
 * @Date: 2021/6/17
 */
@Data
@Accessors(chain = true)
@Alias("GroupLeaveReqVO")
@ApiModel(value = "请求拼团签到", description = "请求拼团签到")
public class GroupLeaveReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String orderGroupDetailId;

    @ApiModelProperty(value = "打卡方式（1.距离打卡 2.扫码打卡）",required = true)
    @NotNull
    private Integer clockType;

    @ApiModelProperty(value = "签到距离")
    private Integer distance;
}