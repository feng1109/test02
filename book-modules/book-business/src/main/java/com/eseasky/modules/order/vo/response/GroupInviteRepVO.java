package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: GroupInviteRepVO
 * @Author lc
 * @Date: 2021/6/18
 */
@Data
@Accessors(chain = true)
@Alias("GroupInviteRepVO")
@ApiModel(value = "响应拼团邀请",description = "响应拼团邀请")
public class GroupInviteRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id")
    private String orderGroupId;

    @ApiModelProperty(value = "座位组id")
    private String seatGroupId;

    @ApiModelProperty(value = "发起人姓名")
    private String userName;

    @ApiModelProperty(value = "座位数量")
    private Integer seatCount;

    @ApiModelProperty(value = "前端展示：预约时间")
    private String orderTime;

    @ApiModelProperty(value = "预约开始时间")
    private String orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private String orderEndTime;

    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间年编号")
    private String roomNum;

    @ApiModelProperty(value = "座位组名称")
    private String groupName;

    @ApiModelProperty(value = "订单状态")
    private Integer listState;

    @ApiModelProperty(value = "参加人员")
    private String[] memberName;

    @ApiModelProperty(value = "截止时间")
    private String cutOffTime;

    @ApiModelProperty(value = "是否迟到")
    private Integer isLate;

    @ApiModelProperty(value = "违约/取消")
    private String remark;
}