package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe: 首页展示响应
 * @title: FirstPageRePVO
 * @Author lc
 * @Date: 2021/4/30
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Alias("FirstPageRePVO")
@ApiModel(value = "首页展示响应", description = "首页展示响应")
public class FirstPageRePVO {

    private static final long serialVersionUID = 1L;

    private String listId;

    private String seatId;

    private Integer orderType;

    private Integer listState;

    @ApiModelProperty(value = "签到方式id")
    private Integer clockType;

    @ApiModelProperty(value = "综合楼物X坐标")
    private String coordx;

    @ApiModelProperty(value = "综合楼物Y坐标")
    private String coordy;

    private Date orderStartTime;

    private Date orderEndTime;

    private String buildName;

    private Integer floorNum;

    private String roomName;

    private String roomNum;

    private String seatNum;

    private String seatGroupName;

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

    @ApiModelProperty(value = "可签到时间")
    private Date ableArriveTime;

    @ApiModelProperty(value = "可暂离时间")
    private Date ableAwayTime;

    @ApiModelProperty(value = "允许迟到时间")
    private Date allowLateTime;

    @ApiModelProperty(value = "暂离后可返回时间")
    private Date ableBackTime;

    @ApiModelProperty(value = "可签退时间")
    private Date ableLeaveTime;

}