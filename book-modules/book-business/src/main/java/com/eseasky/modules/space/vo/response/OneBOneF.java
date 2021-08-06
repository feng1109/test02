package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 综合楼、楼层一对一映射结果
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "综合楼、楼层一对一映射，不包含创建和更新的四个字段", description = "")
public class OneBOneF {

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "综合楼编号")
    private String buildNum;

    @ApiModelProperty(value = "楼层数量")
    private Integer floorCount;

    @ApiModelProperty(value = "综合楼状态：0禁用，1可用")
    private Integer buildState;

    @ApiModelProperty(value = "综合楼配置规则id")
    private String buildConfId;



    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "第几层")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "配置规则id")
    private String floorConfId;

}
