package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: showBlrDetailRepVO
 * @Author lc
 * @Date: 2021/6/10
 */
@Data
@Alias("showBlrDetailRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="响应黑名单规则详情", description="响应黑名单规则详情")
public class ShowBlrDetailRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "黑名单规则id")
    private String blacklistRuleDetailId;

    @ApiModelProperty(value = "预约规则id")
    private String orderRuleId;

    @ApiModelProperty(value = "规则序号")
    private String ruleNum;

    @ApiModelProperty(value = "黑名单规则类型id（待确定）")
    private Integer ruleTypeId;

    @ApiModelProperty(value = "规定天数")
    private Integer ruleLimitDay;

    @ApiModelProperty(value = "规定次数")
    private Integer ruleLimitCount;

    @ApiModelProperty(value = "加入黑名单后生效天数")
    private Integer ruleEffectDay;

    @ApiModelProperty(value = "该规则是否生效（0失效 1生效）")
    private String isEffect;


}