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
 * @title: ShowClockRepVO
 * @Author lc
 * @Date: 2021/4/23
 */
@Data
@Alias("ShowClockRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="查看打卡界面信息", description="查看打卡界面信息")
public class ShowClockRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约座位订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单状态（完成 ，违约 ，待使用等）")
    private Integer listState;

    @ApiModelProperty(value = "预约使用时长（短租，单位：h）")
    private String shortOrderTime;

    @ApiModelProperty(value = "实际使用时间（长租 ，单位：h）")
    private String longUseTime;

    @ApiModelProperty(value = "预约使用开始时间")
    private String orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private String orderEndTime;

    @ApiModelProperty(value = "实际开始结束时间（长短租，长租为上次使用情况）")
    private String useStartTime;

    @ApiModelProperty(value = "实际使用结束时间（长短租，长租为上次使用情况）")
    private String useEndTime;

    @ApiModelProperty(value = "暂离最大时长（min）")
    private Integer awayLimitTime;

    @ApiModelProperty(value = "是否可暂离 0：不可以 1：可以")
    private Integer isAllowAway;

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

    @ApiModelProperty(value = "座位编号")
    private String seatNum;



}