package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe: 用户会议室预约订单列表
 * @title: UserMeetingListVO
 * @Author wpt
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="用户会议室预约记录", description="用户会议室预约记录")
@Accessors(chain = true)
@Alias("UserMeetingInfoRepVO")
public class UserMeetingInfoRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约详情订单id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "预约订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "状态")
    private Integer state;

    @ApiModelProperty(value = "是否评价")
    private Integer isComment;

    @ApiModelProperty(value = "预约时长")
    private String orderTime;

    @ApiModelProperty(value = "签到人数")
    private Long arriveCount;

    @ApiModelProperty(value = "预约使用开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "前端展示开始时间")
    private String startTime;

    @ApiModelProperty(value = "前端展示结束时间")
    private String endTime;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间编号")
    private String roomNum;

    @ApiModelProperty(value = "房间id")
    private String roomId;

}