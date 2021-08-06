package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 审批
 * </p>
 *
 * @author 
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="OrderApprove对象", description="审批")
public class OrderApprove extends Model<OrderApprove> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String approveId;

    @ApiModelProperty(value = "订单id")
    private String orderListId;

    @ApiModelProperty(value = "订单类型 （5种：见字典表）")
    private Integer orderType;

    @ApiModelProperty(value = "申请人id")
    private String userId;

    @ApiModelProperty(value = "申请人姓名")
    private String userName;

    @ApiModelProperty(value = "审批人id")
    private String approveUserId;

    @ApiModelProperty(value = "审批人姓名")
    private String approveUserName;

    @ApiModelProperty(value = "审批时间")
    private Date approveTime;

    @ApiModelProperty(value = "申请时间")
    private Date applyTime;

    @ApiModelProperty(value = "预约开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "申请原因")
    private String reason;

    @ApiModelProperty(value = "地点")
    private String area;

    @ApiModelProperty(value = "审批状态 (1.待审批 2.通过 3.未通过 4.)")
    private Integer approveState;


    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;

    @ApiModelProperty(value = "审批人")
    @TableField(exist = false)
    private List<OrderApprover> orderApprovers= Lists.newArrayList();

    @Override
    protected Serializable pkVal() {
        return this.approveId;
    }

}
