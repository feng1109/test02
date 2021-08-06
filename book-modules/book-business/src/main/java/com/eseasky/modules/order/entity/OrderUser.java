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
 * @since 2021-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderUser对象", description="")
public class OrderUser extends Model<OrderUser> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约人员id")
    @TableId(value = "order_user_id", type = IdType.ASSIGN_UUID)
    private String orderUserId;

    @ApiModelProperty(value = "人员id")
    private String userId;

    @ApiModelProperty(value = "组织id")
    private String orgId;

    @ApiModelProperty(value = "学习总时长（min）")
    private Integer learnTotalTime;

    @ApiModelProperty(value = "本周学习时间（min）")
    private Integer weekLearnTime;

    @ApiModelProperty(value = "单日最长时长（min）")
    private Integer learnMaxTime;

    @ApiModelProperty(value = "违规次数")
    private Integer violateCount;

    @ApiModelProperty(value = "取消次数")
    private Integer cancelCount;


    @ApiModelProperty(value = "黑名单开始时间")
    private Date blacklistStartTime;

    @ApiModelProperty(value = "黑名单结束时间")
    private Date blacklistEndTime;

    @ApiModelProperty(value = "进入黑名单次数")
    private Integer inBlacklistCount;

    @ApiModelProperty(value = "预约次数")
    private Integer orderCount;


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
        return this.orderUserId;
    }

}
