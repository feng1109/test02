package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe:
 * @title: 会议室订单打卡信息
 * @Author wpt
 */
@Data
@Alias("MeetingClockRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="查看打卡界面信息", description="查看打卡界面信息")
public class MeetingClockRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "最新的会议室记录id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "签到状态 1：未到签到时间，2：需要签到，3：未签到，4：已签到")
    private Integer listState;

    @ApiModelProperty(value = "签到状态")
    private Integer signState;

    @ApiModelProperty(value = "订单状态")
    private Integer state;

    @ApiModelProperty(value = "预约使用开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "实际开始结束时间（长短租，长租为上次使用情况）")
    private Date useStartTime;

    @ApiModelProperty(value = "实际使用结束时间（长短租，长租为上次使用情况）")
    private Date useEndTime;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "房间年编号")
    private String roomNum;

    @ApiModelProperty(value = "订单结束时间")
    private String endTime;

    @ApiModelProperty(value = "开始日期")
    private String startDay;

    @ApiModelProperty(value = "订单开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束日期")
    private String endDay;

    @ApiModelProperty(value = "暂离时间")
    private Date awayTime;

    @ApiModelProperty(value = "短租:预约时长")
    private String orderTimeRange;

    @ApiModelProperty(value = "长租:使用总时长")
    private String longUseTime;

    @ApiModelProperty(value = "长租:使用总时长时间戳")
    private Long useTime;

    @ApiModelProperty(value = "可签到时间")
    private Date ableArriveTime;

    @ApiModelProperty(value = "签到方式id")
    private Integer clockType;

    @ApiModelProperty(value = "综合楼物X坐标")
    private String coordx;

    @ApiModelProperty(value = "综合楼物Y坐标")
    private String coordy;
}