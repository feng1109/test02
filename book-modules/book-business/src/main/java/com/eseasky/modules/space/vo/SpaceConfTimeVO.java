package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-可预约时间
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfTime对象", description = "")
public class SpaceConfTimeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "可预约开始时间，HH:mm")
    private String allowStartTime;

    @ApiModelProperty(value = "可预约结束时间，HH:mm")
    private String allowEndTime;

}
