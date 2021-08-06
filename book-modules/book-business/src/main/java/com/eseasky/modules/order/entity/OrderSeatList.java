package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2021-04-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Alias("OrderSeatList")
@ApiModel(value="OrderSeatList对象", description="")
public class OrderSeatList extends Model<OrderSeatList> {

    private static final long serialVersionUID = 1L;



    @ApiModelProperty(value = "预约座位订单id")
    @TableId(value = "order_seat_id",type = IdType.ASSIGN_UUID)
    private String orderSeatId;

    @ApiModelProperty(value = "订单编号")
    private String listNo;


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

    @ApiModelProperty(value = "用户机构id")
    private String userOrgId;

    @ApiModelProperty(value = "班级")
    private String userClasses;

    @ApiModelProperty(value = "专业")
    private String userProfessional;

    @ApiModelProperty(value = "用户机构名")
    private String userOrgName;

    @ApiModelProperty(value = "用户电话")
    private String userPhone;

    @ApiModelProperty(value = "工号")
    private String userNo;

    @ApiModelProperty(value = "用户类型")
    private String userType;

    @ApiModelProperty(value = "微信号")
    private String userWechat;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "建筑组织id")
    private String buildOrgId;

    @ApiModelProperty(value = "订阅类型（1.短租 2.长租）")
    private Integer orderType;

    @ApiModelProperty(value = "预约类型（1.会议室 2.座位预约）")
    private Integer spaceType;

    @ApiModelProperty(value = "订单状态（完成 ，违约 ，待使用等）")
    private Integer listState;

    @ApiModelProperty(value = "自动续约时间阈值（长租，单位：s）")
    private Integer longRequireTime;

    @ApiModelProperty(value = "实际使用时间（长租 ，单位：s）")
    private Integer longUseTime;

    @ApiModelProperty(value = "续约次数")
    private Integer continueCount;


    @ApiModelProperty(value = "预约时间")
    private Date orderTime;

    @ApiModelProperty(value = "是否迟到")
    private Integer isLate;

    @ApiModelProperty(value = "是否早退")
    private Integer isAdvanceLeave;

    @ApiModelProperty(value = "使用日期（方便统计）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date useDay;

    @ApiModelProperty(value = "实际使用总时长（短租，单位：s）")
    private Integer listUseTime;

    @ApiModelProperty(value = "暂离时间（短租）")
    private Integer awayTime;

    @ApiModelProperty(value = "是否订单结束时间（长租，手动取消该订单）")
    private Integer longEndTime;

    @ApiModelProperty(value = "是否提前取消订单（长租）")
    private Integer isCancelLong;

    @ApiModelProperty(value = "预约使用开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约使用结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "实际使用开始时间")
    private Date useStartTime;

    @ApiModelProperty(value = "实际使用结束时间")
    private Date useEndTime;

    @ApiModelProperty(value = "评价")
    private String comment;

    @ApiModelProperty(value = "是否已评价")
    private Integer isComment;


    @ApiModelProperty(value = "评价时间")
    private String commentTime;

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
        return this.orderSeatId;
    }

}
