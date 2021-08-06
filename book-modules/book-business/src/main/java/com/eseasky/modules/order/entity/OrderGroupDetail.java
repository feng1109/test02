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
 * @since 2021-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "OrderGroupDetail对象", description = "")
public class OrderGroupDetail extends Model<OrderGroupDetail> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "拼团预约详情id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String orderGroupDetailId;

    @ApiModelProperty(value = "订单id")
    private String orderGroupId;

    @ApiModelProperty(value = "人员id")
    private String userId;

    @ApiModelProperty(value = "人员姓名")
    private String userName;

    @ApiModelProperty(value = "成员类型")
    private Integer memberType;

    @ApiModelProperty(value = "加入时间")
    private Date joinTime;

    @ApiModelProperty(value = "预约开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "签到时间")
    private Date useStartTime;

    @ApiModelProperty(value = "签退时间")
    private Date useEndTime;

    @ApiModelProperty(value = "学习时长")
    private Integer learnTime;

    @ApiModelProperty(value = "人员订单状态")
    private Integer userState;

    @ApiModelProperty(value = "是否迟到")
    private Integer isLate;

    @ApiModelProperty(value = "是否早退")
    private Integer isAdvanceLeave;

    @ApiModelProperty(value = "是否完成评价")
    private Integer isComment;

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
        return this.orderGroupDetailId;
    }

}
