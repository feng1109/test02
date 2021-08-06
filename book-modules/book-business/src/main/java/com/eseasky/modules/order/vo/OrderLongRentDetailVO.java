package com.eseasky.modules.order.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @describe:
 * @title: OrderLongRentDetailVO
 * @Author lc
 * @Date: 2021/4/29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="OrderLongRentDetail对象", description="")
public class OrderLongRentDetailVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "长租详情id	")
    @TableId("order_long_detail_Id")
    private String orderLongDetailId;

    @ApiModelProperty(value = "订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "使用日期（方便统计）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date useDay;

    @ApiModelProperty(value = "是否签退（0：未签退 1：签退）")
    private Integer isLeave;

    @ApiModelProperty(value = "使用开始时间")
    private LocalDateTime useStartTime;

    @ApiModelProperty(value = "使用结束时间")
    private LocalDateTime useEndTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    @TableLogic(value = "0",delval = "1")
    private String delFlag;


}