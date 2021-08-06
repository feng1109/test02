package com.eseasky.modules.sys.vo;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "修改密码入参")
public class ChangePasswdVO {

    @NotBlank(message = "请填写旧密码")
    @ApiModelProperty(value = "旧密码", required = true)
    private String oldPassword;

    @NotBlank(message = "请填写新密码")
    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;

    @NotBlank(message = "请填写确认密码")
    @ApiModelProperty(value = "确认密码", required = true)
    private String confirmPassword;
}
