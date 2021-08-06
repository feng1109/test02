package com.eseasky.modules.order.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author
 * @since 2021-06-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OrderMeetingUser对象", description = "")
public class OrderMeetingUser extends Model<OrderMeetingUser> {

    private static final long serialVersionUID = -8898344789564303791L;

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "会议室订单id")
    private String orderMeetingId;

    /**
     * 为空是订单对应的参会人数据，不为空是小会议下的参会人数据
     */
    @ApiModelProperty(value = "会议室预约记录id")
    private String orderMeetingInfoId;

    @ApiModelProperty(value = "参会人id")
    private String userId;

//    @ApiModelProperty(value = "参会人签到状态")
//    private String state;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
