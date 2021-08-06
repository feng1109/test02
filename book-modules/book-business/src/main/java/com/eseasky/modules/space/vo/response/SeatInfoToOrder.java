package com.eseasky.modules.space.vo.response;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 根据seatId为订单查询重要信息
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "根据seatId为订单查询重要信息", description = "")
public class SeatInfoToOrder {

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "综合楼归属部门id")
    private String buildDeptId;


    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "第几层")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;


    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号、门牌号")
    private String roomNum;


    @ApiModelProperty(value = "座位id")
    private String seatId;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "座位本身状态：0禁用，1可用")
    private Integer seatState;


    @ApiModelProperty(value = "组id")
    private String groupId;

    @ApiModelProperty(value = "组名称")
    private String groupName;


    @ApiModelProperty(value = "座位的配置规则id")
    private String confId;

    @ApiModelProperty(value = "是否指定人员，默认否0")
    private Integer isPointUser;

    @ApiModelProperty(value = "配置规则归属部门id")
    private String confDeptId;


    @ApiModelProperty(value = "长租形式：每周、每月")
    private Integer longRentType;

    @ApiModelProperty(value = "长租：每天使用阈值")
    private String longRentTime;

    @ApiModelProperty(value = "长租：是否自动续约，默认否0")
    private Integer isAutoRenewal;

    @ApiModelProperty(value = "长租：是否随时打卡，默认否0")
    private Integer isSignRandom;

    @ApiModelProperty(value = "是否座位拼团，默认否0")
    private Integer isGroup;

    @ApiModelProperty(value = "座位拼团限制时间")
    private Integer groupLimitTime;

    @ApiModelProperty(value = "是否需要审批，默认否0")
    private Integer isNeedApprove;


    @ApiModelProperty(value = "人员参加方式：预约人添加，自由参会")
    private Integer meetingJoinType;

    @ApiModelProperty(value = "最大承载人数")
    private Integer meetingJoinCount;

    @ApiModelProperty(value = "签到多少人开启会议")
    private Integer meetingStartCount;

    @ApiModelProperty(value = "多久未开始取消会议")
    private Integer meetingCancelCount;


    @ApiModelProperty(value = "预约：可提前天数")
    private Integer subAdvanceDay;

    @ApiModelProperty(value = "预约：取消多少次后当日不能进行预约")
    private Integer subCancelCount;

    @ApiModelProperty(value = "预约：每天预约次数")
    private Integer subLimitCount;

    @ApiModelProperty(value = "预约：当日允许预约最短时间(min)")
    private Integer subMinTime;

    @ApiModelProperty(value = "预约：当日允许预约最大时间(min)")
    private Integer subMaxTime;


    @ApiModelProperty(value = "签到：迟到多少时间不可签到(min)")
    private Integer signLateTime;

    @ApiModelProperty(value = "签到：提前多少时间可以签到(min)")
    private Integer signAdvanceTime;

    @ApiModelProperty(value = "签到：距签到前多长时间内不能取消预约(min)")
    private Integer signCancelLimitTime;

    @ApiModelProperty(value = "签到：暂离状态超过多长时间不可恢复（min）")
    private Integer signAwayLimitTime;

    @ApiModelProperty(value = "签到：距离场馆坐标范围内多少距离可签到(m)")
    private Integer signMaxDistance;

    @ApiModelProperty(value = "签到：签退限制时间（min）")
    private Integer signLeaveLimitTime;

    @ApiModelProperty(value = "可审批人员id")
    private List<String> approveList = new ArrayList<>();

}
