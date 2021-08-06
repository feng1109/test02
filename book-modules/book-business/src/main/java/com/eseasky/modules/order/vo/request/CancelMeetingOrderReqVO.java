package com.eseasky.modules.order.vo.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @describe:
 * @title: ArriveOrdeReqVO
 * @Author lc
 * @Date: 2021/4/27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "CancelMeetingOrderReqVO")
@ApiModel(value="请求取消会议室预约订单", description="请求取消会议室预约订单")
public class CancelMeetingOrderReqVO implements Serializable {

    private static final long serialVersionUID = -63628445375393400L;

    @ApiModelProperty(value = "订单id",required = true)
    @NotNull
    private String id;

//    @ApiModelProperty(value = "订单类型",required = true)
//    @NotNull
//    private Integer orderType;

}
