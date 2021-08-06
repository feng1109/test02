package com.eseasky.modules.order.vo.request;

import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Alias("OrderFeedBackHandReqVO")
@ApiModel(value = "批量处理VO", description = "批量处理VO")
public class OrderFeedBackHandReqVO extends PageHelper {
    @ApiModelProperty(value = "开始时间")
    private List<String> ids;

}
