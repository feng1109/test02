package com.eseasky.modules.space.vo.response;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 手机预约界面：确认订单时查询座位|座位组|房间返回的对象信息
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "手机预约界面：确认订单时查询座位|座位组|房间返回的对象信息", description = "")
public class ConfirmOrderVO {

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;


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


    @ApiModelProperty(value = "座位id")
    private String seatId;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;


    @ApiModelProperty(value = "座位组id")
    private String groupId;

    @ApiModelProperty(value = "座位组名称")
    private String groupName;

    @ApiModelProperty(value = "座位组中的座位编号")
    private List<String> seatNumList = new ArrayList<String>();


    @ApiModelProperty(value = "预约类型")
    private Integer orderType;


}
