package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: FirstPageReqVO TODO 待删
 * @Author lc
 * @Date: 2021/4/30
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Alias("FirstPageReqVO")
@ApiModel(value = "首页展示请求", description = "首页展示请求")
public class FirstPageReqVO {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "签到方式")
    private Integer clockType;

    @ApiModelProperty(value = "用户id（刚登录）")
    private String  userId;

}