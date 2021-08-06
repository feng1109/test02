package com.eseasky.modules.order.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.text.Format;
import java.util.Date;

/**
 * @describe: 用户订单列表（长租）
 * @title: MyLongRentListVO
 * @Author lc
 * @Date: 2021/4/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="用户预约订单列表", description="用户预约订单列表")
@Accessors(chain = true)
@Alias("UserRentListVO")
public class UserRentListVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约座位订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单状态（完成 ，违约 ，待使用等）")
    private Integer listState;

    @ApiModelProperty(value = "是否评价订单")
    private Integer isComment;

    @ApiModelProperty(value = "预约使用时长（短租，单位：h）")
    private String shortOrderTime;

    @ApiModelProperty(value = "实际使用时间（长租 ，单位：h）")
    private String longUseTime;

    @ApiModelProperty(value = "预约使用开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "实际开始结束时间（长短租，长租为上次使用情况）")
    private Date useStartTime;

    @ApiModelProperty(value = "实际使用结束时间（长短租，长租为上次使用情况）")
    private Date useEndTime;

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

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "座位id")
    private String seatId;

}