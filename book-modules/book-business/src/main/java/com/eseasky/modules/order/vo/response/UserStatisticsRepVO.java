package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: spaceStatisticsRepVO
 * @Author lc
 * @Date: 2021/5/21
 */
@Data
@Alias("UserStatisticsRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="空间统计信息", description="空间统计信息")
public class UserStatisticsRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "人员id")
    private String userId;

    @ApiModelProperty(value = "人员姓名")
    private String userName;

    @ApiModelProperty(value = "人员编号")
    private String userNo;

    @ApiModelProperty(value = "预约总次数")
    private String orderCount;


    @ApiModelProperty(value = "总时长")
    private String learnTotalTime;

    @ApiModelProperty(value = "平均时长")
    private String learnAverageTime;

    @ApiModelProperty(value = "履行次数")
    private Integer useCount;

    @ApiModelProperty(value = "取消次数")
    private String cancelCount;


    @ApiModelProperty(value = "违约次数")
    private String violateCount;


    @ApiModelProperty(value = "黑名单次数")
    private String inBlacklistCount;


}