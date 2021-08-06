package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 手机预约界面：确认订单时查询座位|座位组|房间详情
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "手机预约界面：确认订单时查询座位|座位组|房间详情", description = "")
public class ConfirmOrderParam {

    @NotNull(message = "请填写预约类型")
    @ApiModelProperty(value = "预约类型，1单人短租、2单人长租、3会议室短租、4会议室长租、5拼团", required = true)
    private Integer orderType;

    @NotBlank(message = "请填写id")
    @ApiModelProperty(value = "1和2是seatId，3和4是roomId，5是groupId", required = true)
    private String spaceId;

}
