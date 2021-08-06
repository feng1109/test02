package com.eseasky.modules.space.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 根据规则批量删除已应用的单位
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "根据规则批量删除已应用的单位", description = "")
public class DeleteConfUsedParam {

    @ApiModelProperty(value = "单位id", required = true)
    private String spaceId;

    @ApiModelProperty(value = "单位类型id", required = true)
    private Integer spaceType;

}
