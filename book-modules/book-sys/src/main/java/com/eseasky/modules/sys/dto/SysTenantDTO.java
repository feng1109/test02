package com.eseasky.modules.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "租户管理")
public class SysTenantDTO {

    @ApiModelProperty(value = "租户code")
    private String tenantCode;


    @ApiModelProperty(value = "租户名称")
    private String tenantName;
}
