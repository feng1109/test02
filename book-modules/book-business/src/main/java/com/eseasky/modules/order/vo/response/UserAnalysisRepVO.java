package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe: 空间统计分析
 * @title: SpaceAnalysisRepVO
 * @Author lc
 * @Date: 2021/5/21
 */
@Data
@Alias("UserAnalysisRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="人员统计分析", description="人员统计分析")
public class UserAnalysisRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "空间名")
    private String roomName;

    @ApiModelProperty(value = "预约次数")
    private Integer orderCount;



}