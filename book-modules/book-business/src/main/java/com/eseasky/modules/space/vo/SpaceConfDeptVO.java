package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-可预约部门
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfDept对象", description = "")
public class SpaceConfDeptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "可预约部门id")
    private String deptId;

}
