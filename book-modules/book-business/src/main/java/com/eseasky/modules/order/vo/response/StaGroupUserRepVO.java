package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: StaGroupUserRepVO
 * @Author lc
 * @Date: 2021/7/22
 */
@Data
@Alias("StaGroupUserRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="响应拼团人员信息详情", description="响应拼团人员信息详情")
public class StaGroupUserRepVO {

    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("用户姓名")
    private String userName;

    @ApiModelProperty("学习时间")
    private Integer learnTime;

    @ApiModelProperty("用户状态")
    private Integer userState;
}