package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-可预约人员id
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfUser对象", description = "")
public class SpaceConfUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "指定人员id")
    private String userId;

}
