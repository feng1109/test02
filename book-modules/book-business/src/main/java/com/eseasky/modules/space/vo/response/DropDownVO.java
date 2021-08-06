package com.eseasky.modules.space.vo.response;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DropDownVO下拉框键值对", description = "")
public class DropDownVO {

    @ApiModelProperty(value = "id") // value有的是数字，有的是字符串
    private Object value;

    @ApiModelProperty(value = "内容")
    private String label;

    @ApiModelProperty(value = "子集")
    private List<DropDownVO> children = new ArrayList<>();

}
