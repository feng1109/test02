package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: BlacklistDetailReqVO
 * @Author lc
 * @Date: 2021/6/10
 */
@Data
@Alias(value = "BlacklistDetailReqVO")
@ApiModel(value="请求黑名单详情", description="请求黑名单详情")
public class BlacklistDetailReqVO {

    @ApiModelProperty(value = "黑名单规则详情id")
    private String blacklistRuleDetailId;

    @ApiModelProperty(value = "规则序号",required = true)
    private String ruleNum;

    @ApiModelProperty(value = "黑名单规则类型id（待确定）",required = true)
    private Integer ruleTypeId;

    @ApiModelProperty(value = "规定天数")
    private Integer ruleLimitDay;

    @ApiModelProperty(value = "规定次数",required = true)
    private Integer ruleLimitCount;

    @ApiModelProperty(value = "加入黑名单后生效天数",required = true)
    private Integer ruleEffectDay;

    @ApiModelProperty(value = "该规则是否生效（0失效 1生效）")
    private String isEffect;


}