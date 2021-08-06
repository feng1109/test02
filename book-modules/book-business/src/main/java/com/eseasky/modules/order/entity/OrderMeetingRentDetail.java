package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author
 * @since 2021-06-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OrderMeetingRentDetail对象", description = "")
public class OrderMeetingRentDetail extends Model<OrderMeetingRentDetail> {

    private static final long serialVersionUID = 7251524970969827831L;

    @ApiModelProperty(value = "签到详情id ")
    @TableId(value = "order_meeting_info_id",type = IdType.UUID)
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "签到时间")
    private Date useStartTime;

    @ApiModelProperty(value = "签退")
    private Date useEndTime;

    @ApiModelProperty(value = "实际使用总时长（单位，s）")
    private Integer listUseTime;

    @ApiModelProperty(value = "使用日期（方便统计）")
    private Date useDay;

    @ApiModelProperty(value = "是否签退（0：未签退 1：签退）")
    private Integer isLeave;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return this.orderMeetingInfoId;
    }
}
