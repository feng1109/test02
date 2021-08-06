package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author
 * @since 2021-06-17
 */
@Data
@ApiModel(value = "SpaceConfMenu对象", description = "")
public class SpaceConfMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约类型：1单人短租，2单人长租，3多人短租，4多人长租")
    private Integer orderType;

    @ApiModelProperty(value = "菜单id")
    private String value;

    @ApiModelProperty(value = "菜单名称")
    private String label;

    @ApiModelProperty(value = "默认值")
    private String defaultValue;

    @ApiModelProperty(value = "最小值")
    private String minValue;

    @ApiModelProperty(value = "最大值")
    private String maxValue;

    @ApiModelProperty(value = "开关：0否，1是")
    private Integer state;

}
