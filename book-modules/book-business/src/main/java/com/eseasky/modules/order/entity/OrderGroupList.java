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
import org.apache.ibatis.type.Alias;

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
@Alias(value = "OrderGroupList")
@ApiModel(value="OrderGroupList对象", description="")
public class OrderGroupList extends Model<OrderGroupList> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "多人预约座位订单id")
    @TableId(type = IdType.ASSIGN_UUID)
      private String orderGroupId;

    @ApiModelProperty(value = "订单编号")
    private String listNo;

    @ApiModelProperty(value = "建筑id")
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "座位组id")
    private String seatGroupId;

    @ApiModelProperty(value = "发起人id")
    private String userId;

    @ApiModelProperty(value = "发起人姓名")
    private String userName;

    @ApiModelProperty(value = "发起人组织id")
    private String userOrgId;

    @ApiModelProperty(value = "发起人组织id")
    private String userOrgName;

    @ApiModelProperty(value = "建筑组织id")
    private String buildOrgId;

    @ApiModelProperty(value = "座位数量")
    private Integer seatCount;

    @ApiModelProperty(value = "用户数量")
    private Integer userCount;

    @ApiModelProperty(value = "订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "订单状态")
    private Integer listState;

    @ApiModelProperty(value = "发起拼团时间")
    private Date launchTime;

    @ApiModelProperty(value = "预约（成团）时间")
    private Date teamTime;

    @ApiModelProperty(value = "拼团截止时间")
    private Date cutOffTime;

    @ApiModelProperty(value = "订单开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "订单结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;

    @ApiModelProperty(value = "详情id")
    @TableField(exist = false)
    private String orderGroupDetailId;

    @Override
    protected Serializable pkVal() {
        return this.orderGroupId;
    }

}
