package com.eseasky.modules.order.vo.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * @describe:
 * @title: SpaceInfoRepVO
 * @Author lc
 * @Date: 2021/5/21
 */
@Data
@Alias("SpaceInfoRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="空间信息", description="空间信息")
public class SpaceInfoRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总条数")
    private Integer total;

    @ApiModelProperty(value = "总页数")
    private Integer pages;

    @ApiModelProperty(value = "统计信息")
    private List<SpaceStatisticsRepVO> statistics=Lists.newArrayList();

    @ApiModelProperty(value = "分析信息")
    private List<SpaceAnalysisRepVO> analysis=Lists.newArrayList();



}