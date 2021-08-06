package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: showOftenUseAreaReqVO
 * @Author lc
 * @Date: 2021/7/26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "showOftenUseAreaReqVO")
@ApiModel(value="首页查看推荐场馆", description="首页查看推荐场馆")
public class ShowOftenUseAreaReqVO {

    @ApiModelProperty(value = "经度")
    private String coordx;

    @ApiModelProperty(value = "纬度")
    private String coordy;
}