package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Data
@Alias("OrderMeetingDetailDTO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "会议室预约详情", description = "会议室预约详情")
public class OrderMeetingDetailDTO {
    private static final long serialVersionUID = 8885880670212485891L;

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
    private String userOrgName;

    @ApiModelProperty(value = "建筑名")
    private String buildName;

    @ApiModelProperty(value = "楼层名")
    private String floorName;

    @ApiModelProperty(value = "房间名")
    private String roomName;

    @ApiModelProperty(value = "预约开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "状态")
    private Integer state;

    @ApiModelProperty(value = "使用时长")
    private Integer useTime;

    @ApiModelProperty(value = "备注")
    private String remark;

}
