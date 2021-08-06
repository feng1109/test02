package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;


import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.omg.CORBA.IDLType;

/**
 * <p>
 * 用户反馈
 * </p>
 *
 * @author
 * @since 2021-07-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OrderFeedBack对象", description = "用户反馈")
public class OrderFeedBack extends Model<OrderFeedBack> {

    private static final long serialVersionUID = -6892419803600205518L;

    @ApiModelProperty(value = "反馈id")
    @TableId(value = "feed_back_id",type = IdType.ASSIGN_UUID)
    private String feedBackId;

    @ApiModelProperty(value = "订单id")
    private String orderListId;

    @ApiModelProperty(value = "订单id")
    private Integer orderType;

    @ApiModelProperty(value = "场馆id")
    private String buildId;

    @ApiModelProperty(value = "场馆名")
    private String buildName;

    @ApiModelProperty(value = "场馆栋数")
    private String buildNum;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "楼层层数")
    private String floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号、门牌号")
    private String roomNum;

    @ApiModelProperty(value = "预约人id")
    private String userId;

    @ApiModelProperty(value = "预约人名")
    private String userName;

    @ApiModelProperty(value = "反馈类型")
    private Integer type;

    @ApiModelProperty(value = "状态 0:未处理,1:处理")
    private Integer state;

    @ApiModelProperty(value = "反馈内容")
    private String content;

    @ApiModelProperty(value = "创建人")
    private String createUserId;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    private String updateUserId;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "是否逻辑删除")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
