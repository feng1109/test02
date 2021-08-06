package com.eseasky.modules.sys.vo;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "忘记密码入参")
public class ForgetPasswdVO {

    @NotBlank(message = "请填写手机号")
    @ApiModelProperty(value = "手机号", required = true)
    private String mobile;

    @NotBlank(message = "请填写新密码")
    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;

    @NotBlank(message = "请填写确认密码")
    @ApiModelProperty(value = "确认密码", required = true)
    private String confirmPassword;
    
    @ApiModelProperty(value = "租户，目前不支持多租户，此参数不用传")
    private String tenant;
}
