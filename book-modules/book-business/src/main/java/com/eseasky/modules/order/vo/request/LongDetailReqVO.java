package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: LongDetailReqVO
 * @Author lc
 * @Date: 2021/4/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "LongDetailReqVO")
@ApiModel(value="请求获取长租详情", description="请求获取长租详情")
public class LongDetailReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id")
    private String orderSeatId;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageSize;

}