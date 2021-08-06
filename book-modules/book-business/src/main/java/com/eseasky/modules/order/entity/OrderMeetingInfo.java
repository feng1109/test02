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
 * @since 2021-06-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OrderMeetingInfo对象", description = "")
public class OrderMeetingInfo extends Model<OrderMeetingInfo> {

    @ApiModelProperty(value = "详情id")
    @TableId(value = "order_meeting_info_id", type = IdType.ASSIGN_UUID)
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "订单id")
    private String orderMeetingId;

    @ApiModelProperty(value = "人员id")
    private String userId;

    @ApiModelProperty(value = "人员姓名")
    private String userName;

    @ApiModelProperty(value = "会议开始时间")
    private Date userStartTime;

    @ApiModelProperty(value = "会议结束时间")
    private Date useEndTime;

    @ApiModelProperty(value = "订单状态")
    private Integer state;

    @ApiModelProperty(value = "会议室状态")
    private Integer meetingState;

    @ApiModelProperty(value = "订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "主题")
    private String theme;

    @ApiModelProperty(value = "是否完成评价")
    private Integer isComment;

    @ApiModelProperty(value = "是否预约人")
    private Integer isAppointPerson;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "参会方式 1:发起人添加 2:发起人分享邀请 3:自由参加")
    private Integer attendMeetingWay;

    @ApiModelProperty(value = "参会人员id")
    private String attendMeetingPeople;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return this.orderMeetingInfoId;
    }

}
