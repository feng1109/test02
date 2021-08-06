package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-周几可预约
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfWeek对象", description = "")
public class SpaceConfWeekVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "周几，1到7")
    private String weekId;

}
