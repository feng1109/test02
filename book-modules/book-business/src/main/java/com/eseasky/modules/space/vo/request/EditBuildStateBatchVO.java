package com.eseasky.modules.space.vo.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "批量修改场馆的状态和所属组织", description = "")
public class EditBuildStateBatchVO {

    @NotEmpty(message = "请填写综合楼id")
    @ApiModelProperty(value = "综合楼id集合", required = true)
    private List<String> buildIdList = new ArrayList<String>();

    @ApiModelProperty(value = "综合楼状态，0关闭 1开放")
    private Integer buildState;

    @ApiModelProperty(value = "综合楼所属部门id")
    private String buildDeptId;

}
