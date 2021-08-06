package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
@Accessors(chain = true)
@ApiModel(value="OrderBlacklistRule对象", description="")
public class OrderBlacklistRule extends Model<OrderBlacklistRule> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "黑名单规则id")
    @TableId(value = "blacklist_rule_id",type = IdType.ASSIGN_UUID)
    private String blacklistRuleId;

    @ApiModelProperty(value = "黑名单规则名")
    private String ruleName;

    @ApiModelProperty(value = "创建人id")
    private String userId;

    @ApiModelProperty(value = "创建人姓名")
    private String userName;

    @ApiModelProperty(value = "组织id")
    private String orgId;

    @ApiModelProperty(value = "组织名称")
    private String orgName;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
