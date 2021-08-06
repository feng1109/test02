package com.eseasky.modules.space.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 配置规则
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceConf对象", description = "")
public class SpaceConf extends Model<SpaceConf> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "配置规则id")
    @TableId(value = "conf_id", type = IdType.ASSIGN_UUID)
    private String confId;

    @ApiModelProperty(value = "配置规则名称")
    private String confName;

    @ApiModelProperty(value = "预约类型：1单人短租，2单人长租，3多人短租，4多人长租")
    private Integer orderType;

    @ApiModelProperty(value = "是否指定人员，默认否0")
    private Integer isPointUser;

    @ApiModelProperty(value = "配置规则归属部门id")
    private String confDeptId;

    @ApiModelProperty(value = "长租形式：每周、自然周、每月")
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


    @ApiModelProperty(value = "可预约周")
    private String weekList;

    @ApiModelProperty(value = "可预约日期")
    private String dateList;

    @ApiModelProperty(value = "可预约时间")
    private String timeList;

    @ApiModelProperty(value = "签到方式")
    private String signList;

    @ApiModelProperty(value = "可预约人员类别")
    private String dutyList;

    @ApiModelProperty(value = "可预约部门")
    private String deptList;

    @ApiModelProperty(value = "可预约人员id")
    private String userList;

    @ApiModelProperty(value = "可审批人员id")
    private String approveList;

    @ApiModelProperty(value = "可展示菜单id")
    private String menuList;

    @ApiModelProperty(value = "内置规则不可修改，0否，1是")
    private Integer builtIn;


    @ApiModelProperty(value = "创建人id")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人id")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.confId;
    }

}
