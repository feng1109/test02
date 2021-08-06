package com.eseasky.modules.space.vo.response;

import java.util.List;

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
public class ConfMenuTypeVO {

    @ApiModelProperty(value = "空间属性列表")
    private List<ConfMenuVO> spaceList;

    @ApiModelProperty(value = "预约属性列表")
    private List<ConfMenuVO> orderList;

    @ApiModelProperty(value = "打卡属性列表")
    private List<ConfMenuVO> signList;

}
