package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Alias("OrderMeetingLongSignRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "短租详情", description = "短租详情")
public class OrderMeetingLongSignRepVO {
    private static final long serialVersionUID = 1035165620860206336L;

    @ApiModelProperty(value = "会议室记录id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "开启会议时间")
    private String userStartTime;

    @ApiModelProperty(value = "关闭会议时间")
    private String userEndTime;

    @ApiModelProperty(value = "签到人")
    private List<String> signPeople;
}
