package com.eseasky.modules.space.vo;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 综合楼对象
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "SpaceBuild综合楼对象", description = "")
public class SpaceBuildVO {

    @ApiModelProperty(value = "综合楼id，保存没有修改必传")
    private String buildId;

    @NotBlank(message = "请填写综合楼名称")
    @ApiModelProperty(value = "综合楼名称", required = true)
    private String buildName;

    @ApiModelProperty(value = "综合楼编号")
    private String buildNum;

    @Min(value = 1, message = "请填写楼层数量")
    @ApiModelProperty(value = "楼层数量", required = true)
    private Integer floorCount;

    @ApiModelProperty(value = "综合楼物X坐标")
    private String coordx;

    @ApiModelProperty(value = "综合楼物Y坐标")
    private String coordy;

    @ApiModelProperty(value = "综合楼物位置名称")
    private String coordName;

    @ApiModelProperty(value = "综合楼图片")
    private String buildImage;

    @NotNull(message = "请填写综合楼状态")
    @ApiModelProperty(value = "综合楼状态", required = true)
    private Integer buildState;

    @ApiModelProperty(value = "综合楼归属部门id")
    private String buildDeptId;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "配置规则设置人id")
    private String confUser;

    @ApiModelProperty(value = "配置规则设置日期")
    private Date confTime;

    @ApiModelProperty(value = "场馆使用规则")
    private String useRule;

    @ApiModelProperty(value = "楼层集合")
    private List<SpaceFloorVO> floorList;


    @ApiModelProperty(value = "综合楼状态名称")
    private String buildStateName;

    @ApiModelProperty(value = "综合楼归属部门名称")
    private String buildDeptName;
}
