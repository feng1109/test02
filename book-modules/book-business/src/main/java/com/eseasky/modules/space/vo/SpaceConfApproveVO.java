package com.eseasky.modules.space.vo;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则-审批人员id
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfApprove对象", description = "")
public class SpaceConfApproveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "可审批人员id")
    private String userId;

}
