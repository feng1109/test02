package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 手机端综合楼地图VO
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "手机端综合楼地图VO", description = "")
public class BuildMapVO {

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "综合楼名称", required = true)
    private String buildName;

    @ApiModelProperty(value = "综合楼编号")
    private String buildNum;

    @ApiModelProperty(value = "综合楼物X坐标")
    private String coordx;

    @ApiModelProperty(value = "综合楼物Y坐标")
    private String coordy;

    @ApiModelProperty(value = "综合楼物位置名称")
    private String coordName;

    @ApiModelProperty(value = "综合楼图片")
    private String buildImage;

    @ApiModelProperty(value = "综合楼状态", required = true)
    private Integer buildState;

    @ApiModelProperty(value = "综合楼状态名称")
    private String buildStateName;
}
