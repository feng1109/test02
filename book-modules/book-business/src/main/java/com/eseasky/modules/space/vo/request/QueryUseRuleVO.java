package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 查看使用规则VO
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "查看使用规则VO", description = "")
public class QueryUseRuleVO {

    @NotBlank(message = "请填写综合楼id")
    @ApiModelProperty(value = "综合楼id", required = true)
    private String buildId;

    @ApiModelProperty(value = "使用规则")
    private String useRule;

}
