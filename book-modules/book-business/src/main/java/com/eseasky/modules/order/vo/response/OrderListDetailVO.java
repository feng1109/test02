package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @describe:
 * @title: orderListRepVO
 * @Author lc
 * @Date: 2021/4/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="订单信息详情", description="订单信息详情")
public class OrderListDetailVO {

    @ApiModelProperty(value = "预约座位订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单编号")
    private String listNo;

    @ApiModelProperty(value = "订单状态（完成 ，违约 ，待使用等）")
    private Integer listState;

    @ApiModelProperty(value = "是否迟到")
    private Integer isLate;

//    @ApiModelProperty(value = "预约使用时长（h）")
//    private String orderTimeRange;
//
//    @ApiModelProperty(value = "实际使用时间（长租 ，单位：s）")
//    private Integer useTime;

    @ApiModelProperty(value = "预约时间")
    private Date orderTime;


    @ApiModelProperty(value = "预约使用开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "签到时间")
    private Date useStartTime;

    @ApiModelProperty(value = "签退时间")
    private Date useEndTime;


    @ApiModelProperty(value = "前端展示日期")
    private String time;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间年编号")
    private String roomNum;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;
}