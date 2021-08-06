package com.eseasky.modules.order.vo.request;

import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * UserMeetingOrderReqVO
 * @author 王鹏滔
 * @date
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "UserMeetingOrderReqVO")
@ApiModel(value="获取用户会议室预约订单", description="获取用户会议室预约订单")
public class UserMeetingOrderReqVO extends PageHelper implements Serializable {

    private static final long serialVersionUID = 1799918994399753031L;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "时间跨度（单位:月 /0代表无时间限制）",required = true)
    @NotNull
    private Integer timeRange;

    @ApiModelProperty(value = "截止时间")
    private Date endTime;

    @ApiModelProperty(value = "订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单id集合")
    private List<String> meetingIds;
}