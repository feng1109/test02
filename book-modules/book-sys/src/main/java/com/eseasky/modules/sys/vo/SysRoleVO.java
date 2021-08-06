package com.eseasky.modules.sys.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@ApiModel(value = "角色管理")
@Data
public class SysRoleVO {

    @NotBlank(message = "角色不能为空")
    @ApiModelProperty(value = "角色id")
    private String roleId;

    @NotEmpty(message = "用户不能为空")
    @ApiModelProperty(value = "用户id",dataType = "Array")
    private Set<String> userIds;




}
