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
 * @since 2021-05-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderNotice对象", description="")
public class OrderNotice extends Model<OrderNotice> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约通知id")
    @TableId(value = "order_notice_id",type = IdType.ASSIGN_UUID)
    private String orderNoticeId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "通知类型id")
    private Integer noticeType;

    @ApiModelProperty(value = "标题类型id")
    private Integer titleType;

    @ApiModelProperty(value = "内容类型id")
    private Integer contentType;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "是否已读（0：未读 1：已读）")
    private Integer isRead;

    @ApiModelProperty(value = "推送时间")
    private Date sendTime;

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
        return this.orderNoticeId;
    }

}
