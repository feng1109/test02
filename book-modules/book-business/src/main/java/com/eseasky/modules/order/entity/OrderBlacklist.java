package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="OrderBlacklist对象", description="")
public class OrderBlacklist extends Model<OrderBlacklist> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "黑名单id")
    @TableId(value = "order_blacklist_id",type = IdType.ASSIGN_UUID)
    private String orderBlacklistId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "组织id（建筑所属组织）")
    private String buildOrgId;

    @ApiModelProperty(value = "黑名单开始时间")
    private Date startTime;

    @ApiModelProperty(value = "黑名单结束时间")
    private Date endTime;

    @ApiModelProperty(value = "黑名单持续时间（单位：s）")
    private Integer continueTime;

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
