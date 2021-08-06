package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @describe:
 * @title: OrderMeetingListDetailRepVO
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="会议室订单信息详情", description="会议室订单信息详情")
public class OrderMeetingListDetailRepVO {

    @ApiModelProperty(value = "会议室预约订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "最新的会议室记录id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "订单编号")
    private String theme;

    @ApiModelProperty(value = "参会人集合")
    private List<String> attendMeetingPeople;

    @ApiModelProperty(value = "参会人ID集合")
    private String attendMeetingPeopleStr;

    @ApiModelProperty(value = "会议室状态")
    private Integer state;

    @ApiModelProperty(value = "签到状态")
    private Integer signState;

    @ApiModelProperty(value = "签到时间")
    private Date signTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "预约使用开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "实际使用开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "实际使用结束时间")
    private Date useEndTime;

    @ApiModelProperty(value = "前端展示日期")
    private String time;

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
}
