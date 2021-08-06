package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 综合楼、楼层、空间一对一映射结果
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "综合楼、楼层、空间一对一映射", description = "")
public class OneBOneFOneR {

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



    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号、门牌号")
    private String roomNum;

    @ApiModelProperty(value = "空间平面图")
    private String roomImage;

    @ApiModelProperty(value = "空间所属区域")
    private String area;

    @ApiModelProperty(value = "空间状态：0禁用，1可用")
    private Integer roomState;

    @ApiModelProperty(value = "空间配置规则id")
    private String roomConfId;

    @ApiModelProperty(value = "座位可用和禁用总数")
    private Integer seatCount;

    @ApiModelProperty(value = "座位非禁用总数")
    private Integer seatNotForbidCount;

}
