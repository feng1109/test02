package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe:
 * @title: ListCommentRepVO
 * @Author lc
 * @Date: 2021/4/21
 */
@Data
@Alias("ListCommentRepVO")
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="订单评价", description="订单评价")
public class ListCommentRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单id")
    private String orderSeatId;

    @ApiModelProperty("用户姓名")
    private String userName;

    @ApiModelProperty("评价")
    private String comment;

    @ApiModelProperty("评价时间")
    private Date commentTime;


}