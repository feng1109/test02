package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 *
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Alias("OrderFeedBackAddReqVO")
@ApiModel(value = "提交反馈请求vo", description = "提交反馈请求vo")
public class OrderFeedBackAddReqVO {
    @ApiModelProperty(value = "订单id")
    @NotNull(message = "订单id不能传空")
    private String orderListId;

    @ApiModelProperty(value = "订单类型")
    @NotNull(message = "订单类型不能传空")
    private Integer orderType;

    @ApiModelProperty(value = "反馈类型")
    @NotNull(message = "反馈类型不能传空")
    private Integer type;

    @ApiModelProperty(value = "反馈内容")
    private String content;
}
