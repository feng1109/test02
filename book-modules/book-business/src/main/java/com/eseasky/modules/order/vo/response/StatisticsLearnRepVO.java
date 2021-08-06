package com.eseasky.modules.order.vo.response;

import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.HashMap;

/**
 * @describe:
 * @title: StatisticsLearnRepVO
 * @Author lc
 * @Date: 2021/5/7
 */
@Data
@Alias("StatisticsLearnRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="统计界面返回对象", description="统计界面返回对象")
public class StatisticsLearnRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "学习总时长（h）")
    private String learnTotalTime;

    @ApiModelProperty(value = "累计预约次数")
    private Integer orderCount;

    @ApiModelProperty(value = "本月总时长 (h)")
    private String learnMonthTime;

    @ApiModelProperty(value = "本月单次最长时长 (h)")
    private String longestMonthTime;

    @ApiModelProperty(value = "本月预约次数 ")
    private Integer orderMonthCount;

    @ApiModelProperty(value = "本月违约次数")
    private Integer violateCount;

    @ApiModelProperty(value = "履约次数")
    private Integer useCount;

    @ApiModelProperty(value = "本月进入黑名单次数")
    private Integer inBlCount;


    @ApiModelProperty(value = "每日学习时间")
    private HashMap dayLearnTime= Maps.newHashMap();
}