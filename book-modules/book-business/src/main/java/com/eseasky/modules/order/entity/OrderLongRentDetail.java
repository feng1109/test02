package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
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
 * @since 2021-04-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderLongRentDetail对象", description="")
public class OrderLongRentDetail extends Model<OrderLongRentDetail> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "长租详情id	")
    @TableId(value = "order_long_detail_Id",type = IdType.ASSIGN_UUID)
    private String orderLongDetailId;

    @ApiModelProperty(value = "订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "使用日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date useDay;

    @ApiModelProperty(value = "是否签退（0：未签退 1：签退）")
    private Integer isLeave;

    @ApiModelProperty(value = "实际使用总时长（单位：s）")
    private Integer listUseTime;

    @ApiModelProperty(value = "使用开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "使用结束时间")
    private Date useEndTime;

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
        return this.orderLongDetailId;
    }

}
