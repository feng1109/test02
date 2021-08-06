package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.Delete;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2021-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderViolateDetail对象", description="")
public class OrderViolateDetail extends Model<OrderViolateDetail> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "违约记录id")
    @TableId(value = "violate_list_id",type = IdType.ASSIGN_UUID)
    private String violateListId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "建筑机构id")
    private String buildOrgId;

    @ApiModelProperty(value = "违规时间")
    private Date violateTime;

    @ApiModelProperty(value = "黑名单规则违约类型")
    private Integer blacklistRuleType;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    @TableLogic(value = "0",delval = "1")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
