package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;


@Data
@Alias("OrderGroupListDetailedDTO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "拼团信息详细信息", description = "拼团信息详细信息")
public class OrderGroupListDetailedDTO {

    @ApiModelProperty(value = "订单编号")
    private String listNo;

    @ApiModelProperty(value = "座位组id")
    private String seatGroupId;

    @ApiModelProperty(value = "发起人id")
    private String userId;

    @ApiModelProperty(value = "发起人姓名")
    private String userName;

    @ApiModelProperty(value = "建筑组织id")
    private String buildOrgId;

    @ApiModelProperty(value = "座位数量")
    private Integer seatCount;

    @ApiModelProperty(value = "用户数量")
    private Integer userCount;

    @ApiModelProperty(value = "订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单状态")
    private Integer listState;

    @ApiModelProperty(value = "发起拼团时间")
    private Date launchTime;

    @ApiModelProperty(value = "预约（成团）时间")
    private Date teamTime;

    @ApiModelProperty(value = "拼团截止时间")
    private Date cutOffTime;

    @ApiModelProperty(value = "订单开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "订单结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "详情id")
    private String orderGroupDetailId;

    @ApiModelProperty(value = "建筑id")
    private String buildId;

    @ApiModelProperty(value = "建筑num")
    private String buildNum;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间年编号")
    private String roomNum;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;
}
