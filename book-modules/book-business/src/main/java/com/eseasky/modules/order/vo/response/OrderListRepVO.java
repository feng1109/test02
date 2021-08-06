package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe: 预约详情
 * @title: OrderListRepVO
 * @Author lc
 * @Date: 2021/5/24
 */
@Data
@Alias("OrderListRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="响应预约订单", description="响应预约订单")
public class OrderListRepVO {

    @ApiModelProperty(value = "订单id")
    private String listId;

    @ApiModelProperty(value = "订单编号")
    private String listNo;

    @ApiModelProperty(value = "所属组织")
    private String orgName;

    @ApiModelProperty(value = "预约类型")
    private String orderType;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "人员类型")
    private String userType;

    @ApiModelProperty(value = "学号")
    private String userNo;

    @ApiModelProperty(value = "发起预约时间")
    private Date orderTime;

    @ApiModelProperty(value = "预约时间(前端展示)")
    private String showTime;

    @ApiModelProperty(value = "预约开始时间")
    private String orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private String orderEndTime;

    @ApiModelProperty(value = "订单状态")
    private String listState;

    @ApiModelProperty(value = "地点")
    private String area;

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