package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderNoticeMeetingDTO", description="会议室预约内容")
public class OrderNoticeDTO {

    @ApiModelProperty(value = "会议室预约订单id")
    private String orderListId;

    @ApiModelProperty(value = "最新的会议室记录id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

//    @ApiModelProperty(value = "订单编号")
//    private String orderNo;

    @ApiModelProperty(value = "会议室主题")
    private String theme;

//    @ApiModelProperty(value = "会议室状态")
//    private String listState;

    @ApiModelProperty(value = "审核状态 1：通过，2：不通过")
    private String approveState;

    @ApiModelProperty(value = "预约使用开始时间")
    private String orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private String orderEndTime;

    @ApiModelProperty(value = "预约时长")
    private String orderTime;

    @ApiModelProperty(value = "通知内容")
    private String noticeContent;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间编号")
    private String roomNum;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;
}
