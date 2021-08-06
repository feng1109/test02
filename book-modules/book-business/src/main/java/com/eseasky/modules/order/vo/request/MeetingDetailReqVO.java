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
@Alias(value = "MeetingDetailReqVO")
@ApiModel(value="会议室订单详情", description="会议室订单详情")
public class MeetingDetailReqVO extends PageHelper {

    private static final long serialVersionUID = -7168704899438674900L;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "用户id" )
    private String userId;

}