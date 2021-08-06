package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @describe:
 * @title: BuilderVO
 * @Author lc
 * @Date: 2021/4/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="建筑订单信息", description="建筑订单信息")
public class BuilderDetailRepVO {

    @ApiModelProperty(value = "建筑id")
    private String buildId;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "建筑编号")
    private String buildNum;

    @ApiModelProperty(value = "建筑状态")
    private Integer buildState;

    @ApiModelProperty(value = "建筑图片")
    private String buildImage;

    @ApiModelProperty(value = "总座位数量")
    private String buildSeatCount;

    @ApiModelProperty(value = "今日订单数")
    private Integer todayListCount;

    @ApiModelProperty(value = "被占座位数")
    private Integer  buildUsedSeat;

}