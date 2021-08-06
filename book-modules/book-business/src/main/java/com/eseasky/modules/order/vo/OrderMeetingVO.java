package com.eseasky.modules.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * <p>
 * 会议室预约订单 服务类
 * </p>
 *
 * @author 王鹏滔
 * @since 2021-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="会议室预约订单VO", description="会议室预约订单VO")
public class OrderMeetingVO implements Serializable {
    private static final long serialVersionUID = 7693762160118435816L;

    @ApiModelProperty(value = "id")
    private String orderMeetingId;

    @ApiModelProperty(value = "创建人")
    private String createUserId;

    @ApiModelProperty(value = "创建人名")
    private String createUserName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    private String updateUserId;

    @ApiModelProperty(value = "更新人名")
    private String updateUserName;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "建筑id")
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "座位id")
    private String seatId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "用户电话")
    private String userPhone;

    @ApiModelProperty(value = "工号")
    private String userNo;

    @ApiModelProperty(value = "用户类型 1.学生 2.教师")
    private Integer userType;

    @ApiModelProperty(value = "微信号")
    private String userWechat;

    @ApiModelProperty(value = "订单类型 （1.短租 2.长租 3.短租会议室 4.长租会议室）")
    private Integer orderType;

    @ApiModelProperty(value = "订单状态 1:待开始 2:使用中 3:已取消 4:已完成")
    private Integer state;

    @ApiModelProperty(value = "续约次数")
    private Integer continueCount;

    @ApiModelProperty(value = "续约时间阈值 单位：s")
    private Integer renewLimitTime;

    @ApiModelProperty(value = "是否提前取消订单")
    private Integer isAdvanceCancel;

    @ApiModelProperty(value = "是否需要审批")
    private Integer isRequireApproval;

    @ApiModelProperty(value = "是否迟到")
    private Integer isLate;

    @ApiModelProperty(value = "是否早退")
    private Integer isAdvanceLeave;

    @ApiModelProperty(value = "预约开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "实际开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "实际结束时间")
    private Date useEndTime;

    @ApiModelProperty(value = "是否评价")
    private Integer isComment;

    @ApiModelProperty(value = "主题")
    private String theme;

    @ApiModelProperty(value = "参会方式 1:发起人添加 2:发起人分享邀请 3:自由参加")
    private Integer attendMeetingWay;

    @ApiModelProperty(value = "参会人id集合")
    private List<String> attendMeetingPeople;

    @ApiModelProperty(value = "是否删除")
    private Integer delFlag;

}
