package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import org.checkerframework.checker.units.qual.A;

import java.util.Date;

/**
 * @describe:
 * @title: GroupCreateReqVO
 * @Author lc
 * @Date: 2021/6/15
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Alias("GroupCreateReqVO")
@ApiModel(value = "生成分组预约订单", description = "生成分组预约订单")
public class GroupCreateReqVO {

    @ApiModelProperty(value = "预约开始时间",required = true)
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间",required = true)
    private Date orderEndTime;

    @ApiModelProperty(value = "座位组id",required = true)
    private String seatGroupId;

    @ApiModelProperty(value = "座位数量",required = true)
    private Integer seatCount;

}