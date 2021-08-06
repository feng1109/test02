package com.eseasky.modules.order.vo.response;

import com.google.gson.JsonObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("OrderMeetingLongDetailRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "长租详情", description = "长租详情")
public class OrderMeetingLongDetailRepVO {
    private static final long serialVersionUID = -6246978386236537182L;

    @ApiModelProperty(value = "会议室预约订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "预约人id")
    private String userId;

    @ApiModelProperty(value = "预约人名")
    private String userName;

    @ApiModelProperty(value = "用户电话")
    private String userPhone;

    @ApiModelProperty(value = "人员类型")
    private String userType;

    @ApiModelProperty(value = "学号")
    private String userNo;

    @ApiModelProperty(value = "所属组织")
    private List<String> userOrgName;

    @ApiModelProperty(value = "建筑名")
    private String buildName;

    @ApiModelProperty(value = "楼层名")
    private String floorName;

    @ApiModelProperty(value = "房间名")
    private String roomName;

    @ApiModelProperty(value = "预约开始时间")
    private String orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private String orderEndTime;

    @ApiModelProperty(value = "前端展示，带~")
    private String timeShow;

    @ApiModelProperty(value = "状态")
    private Integer state;

    @ApiModelProperty(value = "使用时长")
    private String useTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "签到记录")
    private List<OrderMeetingLongSignRepVO> signList;
}
