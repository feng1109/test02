package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则已使用单位列表
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "配置规则已使用单位列表", description = "")
public class QueryConfUsedParam {

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "第几页，至少一页", required = true)
    private long pageNum;

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "每页数量，至少一条", required = true)
    private long pageSize;

    @NotBlank(message = "请填写配置规则id")
    @ApiModelProperty(value = "配置规则id", required = true)
    private String confId;

    @ApiModelProperty(value = "单位名称")
    private String spaceName;

}
