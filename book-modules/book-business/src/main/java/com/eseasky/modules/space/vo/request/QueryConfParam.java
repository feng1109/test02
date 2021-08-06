package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.Min;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则列表查询参数
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "配置规则列表查询参数", description = "")
public class QueryConfParam {

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "第几页，至少一页", required = true)
    private long pageNum;

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "每页数量，至少一条", required = true)
    private long pageSize;

    @ApiModelProperty(value = "预约类型：1单人短租，2单人长租，3多人短租，4多人长租")
    private Integer orderType;

    @ApiModelProperty(value = "配置规则归属部门id")
    private String confDeptId;

    @ApiModelProperty(value = "配置规则名称")
    private String confName;

}
