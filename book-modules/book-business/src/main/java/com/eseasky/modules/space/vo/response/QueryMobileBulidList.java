package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 手机端综合楼查询列表
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "手机端综合楼查询列表", description = "")
public class QueryMobileBulidList {

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "综合楼编号")
    private String buildNum;

    @ApiModelProperty(value = "综合楼图片")
    private String buildImage;

    @ApiModelProperty(value = "综合楼状态，0关闭，1开放")
    private Integer buildState;

    @ApiModelProperty(value = "综合楼座位总数")
    private Integer seatTotal = 0;

    @ApiModelProperty(value = "综合楼座位已占用总数")
    private Integer seatInUsedTotal = 0;

    @ApiModelProperty(value = "综合楼座位可使用总数")
    private Integer seatNotUsedTotal = 0;

    @ApiModelProperty(value = "综合楼座位今日预约总数")
    private Integer seatOrderTotal = 0;


    @ApiModelProperty(value = "综合楼空间总数")
    private Integer roomTotal = 0;

    @ApiModelProperty(value = "综合楼空间可使用总数")
    private Integer roomNotUsedTotal = 0;

}
