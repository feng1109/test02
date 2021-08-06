package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe: 用户订单列表（长租）
 * @title: MyLongRentListVO
 * @Author lc
 * @Date: 2021/4/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="用户拼团预约订单列表", description="用户拼团预约订单列表")
@Accessors(chain = true)
@Alias("UserGroupRentListVO")
public class UserGroupRentListVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约拼团订单id")
    private String orderGroupDetailId;

    @ApiModelProperty(value = "订单状态（完成 ，违约 ，待使用等）")
    private Integer listState;

    @ApiModelProperty(value = "是否评价订单")
    private Integer isComment;

    @ApiModelProperty(value = "预约使用时长（单位：h）")
    private String shortOrderTime;

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

    @ApiModelProperty(value = "座位组名称")
    private String seatGroupName;

    @ApiModelProperty(value = "座位组id")
    private String seatGroupId;

}