package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 根据seatId为订单查询重要信息
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "根据seatId为订单查询重要信息", description = "")
public class SeatInfoForQuickOrder {

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "综合楼状态")
    private Integer buildState;


    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "第几层")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;


    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号、门牌号")
    private String roomNum;

    @ApiModelProperty(value = "空间状态")
    private Integer roomState;

    @ApiModelProperty(value = "配置id")
    private String confId;


    @ApiModelProperty(value = "座位id")
    private String seatId;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "座位本身状态：0禁用，1可用")
    private Integer seatState;


    @ApiModelProperty(value = "预约类型", required = true)
    private Integer orderType;

    @ApiModelProperty(value = "预约日期，yyyy-MM-dd", required = true)
    private String startDate;

    @ApiModelProperty(value = "预约日期，yyyy-MM-dd", required = true)
    private String endDate;

    @ApiModelProperty(value = "预约开始时间，HH:mm", required = true)
    private String startTime;

    @ApiModelProperty(value = "预约开始时间，HH:mm", required = true)
    private String endTime;

}
