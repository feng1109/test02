package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="OrderBlacklistRuleDetail对象", description="")
public class OrderBlacklistRuleDetail extends Model<OrderBlacklistRuleDetail> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "黑名单规则详情id")
    @TableId(value = "blacklist_rule_detail_id",type = IdType.ASSIGN_UUID)
    private String blacklistRuleDetailId;

    @ApiModelProperty(value = "黑名单规则id")
    private String blacklistRuleId;

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

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return this.blacklistRuleDetailId;
    }

}
