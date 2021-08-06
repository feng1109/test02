package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe:
 * @title: JoinGroupReqVO
 * @Author lc
 * @Date: 2021/6/17
 */
@Data
@Accessors(chain = true)
@Alias("JoinGroupReqVO")
@ApiModel(value = "请求加入拼团", description = "请求加入拼团")
public class GroupJoinReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id", required = true)
    private String orderGroupId;


}