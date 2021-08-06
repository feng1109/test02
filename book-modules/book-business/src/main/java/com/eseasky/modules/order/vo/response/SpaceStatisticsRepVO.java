package com.eseasky.modules.order.vo.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * @describe:
 * @title: spaceStatisticsRepVO
 * @Author lc
 * @Date: 2021/5/21
 */
@Data
@Alias("spaceStatisticsRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="空间统计信息", description="空间统计信息")
public class SpaceStatisticsRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间名称")
    private String roomId;

    @ApiModelProperty(value = "预约总次数")
    private Integer totalCount;

    @ApiModelProperty(value = "平均总次数")
    private String averageCount;

    @ApiModelProperty(value = "总时长")
    private String totalTime;

    @ApiModelProperty(value = "平均时长")
    private String averageTime;


}