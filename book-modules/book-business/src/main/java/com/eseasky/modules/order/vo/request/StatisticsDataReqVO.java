package com.eseasky.modules.order.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.ibatis.type.Alias;
import org.checkerframework.checker.units.qual.A;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @describe: 展示pc端统计数据
 * @title: StatisticsDataReqVO
 * @Author lc
 * @Date: 2021/5/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "StatisticsDataReqVO")
@ApiModel(value="请求查看pc端统计数据", description="请求查看pc端统计数据")
public class StatisticsDataReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "统计类型")
    @NotNull
    private Integer statisticsType;

    @ApiModelProperty(value = "月份")
    @NotNull
    private String month;



}