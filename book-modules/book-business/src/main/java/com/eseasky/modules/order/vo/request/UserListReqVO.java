package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @describe:
 * @title: UserListReqVO
 * @Author lc
 * @Date: 2021/5/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "UserListReqVO")
@ApiModel(value="请求获取用户预约订单", description="请求获取用户预约订单")
public class UserListReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "时间跨度（单位:月 /0代表无时间限制）",required = true)
    @NotNull
    private Integer timeRange;

    @ApiModelProperty(value = "截止时间")
    private Date endTime;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    private Integer pageSize;

}