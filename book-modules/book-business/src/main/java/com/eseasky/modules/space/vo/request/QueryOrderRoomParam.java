package com.eseasky.modules.space.vo.request;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 为多人预约查询空间
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "为多人预约查询空间", description = "")
public class QueryOrderRoomParam {

    @ApiModelProperty(value = "空间id", required = true)
    private String roomId;

    @ApiModelProperty(value = "预约类型", required = true)
    private Integer orderType;

    @ApiModelProperty(value = "预约开始日期，yyyy-MM-dd HH:mm", required = true)
    private Date startDate;

    @ApiModelProperty(value = "预约结束日期，yyyy-MM-dd HH:mm", required = true)
    private Date endDate;

}
