package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-可预约人员类别Id
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfDuty对象", description = "")
public class SpaceConfDutyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "职务id")
    private String dutyId;

}
