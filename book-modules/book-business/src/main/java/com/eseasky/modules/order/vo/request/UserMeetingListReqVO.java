package com.eseasky.modules.order.vo.request;

import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * UserMeetingListReqVO
 * @author 王鹏滔
 * @date
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "UserMeetingListReqVO")
@ApiModel(value="获取用户会议室预约记录", description="获取用户会议室预约记录")
public class UserMeetingListReqVO extends PageHelper implements Serializable {

    private static final long serialVersionUID = 1799918994399753031L;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "会议室订单id")
    private String orderMeetingId;
}