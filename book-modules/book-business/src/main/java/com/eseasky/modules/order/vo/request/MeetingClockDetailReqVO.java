package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * MeetingLongDetailReqVO
 * @Author: 王鹏滔
 * @Date: 2021/6/10 16:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "MeetingLongDetailReqVO")
@ApiModel(value="请求获取会议室打卡信息", description="请求获取会议室打卡信息")
public class MeetingClockDetailReqVO {

    private static final long serialVersionUID = -3449423157993895893L;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingInfoId;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageSize;

}