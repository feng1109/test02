package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-签到方式
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfSign对象", description = "")
public class SpaceConfSignVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "签到方式id")
    private String signId;

}
