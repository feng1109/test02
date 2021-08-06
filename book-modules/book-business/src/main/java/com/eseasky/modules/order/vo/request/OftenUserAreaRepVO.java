package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: OftenUserAreaRepVO
 * @Author lc
 * @Date: 2021/6/22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "OftenUserAreaRepVO")
@Accessors(chain = true)
@ApiModel(value="请求常用地点", description="请求常用地点")
public class OftenUserAreaRepVO {

    @ApiModelProperty("常用地点")
    private String area;

    @ApiModelProperty("建筑id")
    private String buildId;

    @ApiModelProperty("使用次数")
    private String useCount;

    @ApiModelProperty("类型 1.常用 2.距离近")
    private Integer type;

    @ApiModelProperty("距离")
    private Integer distance;
}