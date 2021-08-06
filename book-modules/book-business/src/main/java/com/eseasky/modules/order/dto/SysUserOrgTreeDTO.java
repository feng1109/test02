package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SysUserOrgTreeData用户树形数据", description = "")
public class SysUserOrgTreeDTO {

    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "父id")
    private String pid;

    @ApiModelProperty(value = "true 用户， false 组织")
    private Boolean user;

    @ApiModelProperty(value = "用户数量")
    private Integer memberCount;

    @ApiModelProperty(value = "children")
    private List<SysUserOrgTreeDTO> children;
}
