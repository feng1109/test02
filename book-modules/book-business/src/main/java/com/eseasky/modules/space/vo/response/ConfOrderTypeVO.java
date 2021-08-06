package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 4种预约规则
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "4种预约规则", description = "")
public class ConfOrderTypeVO {

    @ApiModelProperty(value = "单人短租")
    private ConfMenuTypeVO singleOnce;

    @ApiModelProperty(value = "单人长租")
    private ConfMenuTypeVO singleLong;

    @ApiModelProperty(value = "多人短租")
    private ConfMenuTypeVO multiOnce;

    @ApiModelProperty(value = "多人长租")
    private ConfMenuTypeVO multiLong;

}
