package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * @since 2021-05-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderComment对象", description="")
public class OrderComment extends Model<OrderComment> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约评论id")
    @TableId(value = "order_comment_id",type = IdType.ASSIGN_UUID)
    private String orderCommentId;

    @ApiModelProperty(value = "预约订单id")
    private String orderListId;

    @ApiModelProperty(value = "订单类型")
    private Integer orderType;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "建筑id")
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "评论等级")
    private Integer level;



    @ApiModelProperty(value = "评论内容")
    private String content;

    @ApiModelProperty(value = "评价时间")
    private Date commentTime;

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
        return this.orderCommentId;
    }

}
