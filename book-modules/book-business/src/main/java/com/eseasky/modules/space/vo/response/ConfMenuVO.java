package com.eseasky.modules.space.vo.response;

import java.util.ArrayList;
import java.util.List;

import com.eseasky.modules.space.vo.SpaceConfMenuVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 每种规则列表
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "每种规则列表", description = "")
public class ConfMenuVO {

    @ApiModelProperty(value = "id") // value有的是数字，有的是字符串
    private Object value;

    @ApiModelProperty(value = "内容")
    private String label;

    @ApiModelProperty(value = "内容")
    private SpaceConfMenuVO menu;

    @ApiModelProperty(value = "子集")
    private List<DropDownVO> children = new ArrayList<>();

}
