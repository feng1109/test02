package com.eseasky.modules.order.vo.request;

import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * MeetingClockReqVO
 * @Author: 王鹏滔
 * @Date: 2021/6/9 15:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "MeetingClockReqVO")
@ApiModel(value="查看打卡界面", description="查看打卡界面")
public class MeetingClockReqVO extends PageHelper {

    private static final long serialVersionUID = -7168704899438674900L;

    @ApiModelProperty(value = "订单详情id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "用户id" )
    private String userId;

    @ApiModelProperty(value = "打卡方式")
    private Integer clockType;

    @ApiModelProperty(value = "查询条件：订单id/人名")
    private String queryCondition;

}