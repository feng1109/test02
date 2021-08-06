package com.eseasky.modules.order.vo.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @describe:
 * @title: BuilderTotalVO
 * @Author lc
 * @Date: 2021/4/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="建筑订单信息总览", description="建筑订单总信息总览")
public class BuilderTotalRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总座位数")
    private Integer totalSeat;

    @ApiModelProperty(value = "被占座位数")
    private Integer UsedSeat;

    @ApiModelProperty(value = "建筑预约详情")
    private List<BuilderDetailRepVO> builderDetailRepVOList = Lists.newArrayList();

}