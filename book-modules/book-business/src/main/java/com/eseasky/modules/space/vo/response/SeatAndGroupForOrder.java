package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 根据seatId为订单查询重要信息
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "根据seatId为订单查询重要信息", description = "")
public class SeatAndGroupForOrder {

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "组名称")
    private String groupName;

    @ApiModelProperty(value = "签到方式id")
    private String signId;

}
