package com.eseasky.modules.order.vo.request;

import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 *
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Alias("OrderFeedBackPageReqVO")
@ApiModel(value = "反馈分页查询vo", description = "反馈分页查询vo")
public class OrderFeedBackPageReqVO extends PageHelper {
    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "类型")
    private Integer type;

    @ApiModelProperty(value = "场馆id")
    private String buildId;

    @ApiModelProperty(value = "状态排序,1:降序,2:升序")
    private Integer isOrder;

}
