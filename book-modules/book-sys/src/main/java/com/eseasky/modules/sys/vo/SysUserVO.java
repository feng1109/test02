package com.eseasky.modules.sys.vo;

import com.google.common.collect.Sets;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

@ApiModel(value = "批量修改部门")
@Data
public class SysUserVO {

    @ApiModelProperty(value = "用户id")
    private Set<String> userIds = Sets.newHashSet();

    @ApiModelProperty(value = "部门id")
    private Set<String> orgIds = Sets.newHashSet();
}
