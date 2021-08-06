package com.eseasky.modules.order.vo.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.eseasky.modules.order.dto.OrderNoticeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "OrderNoticeRepVO")
@ApiModel(value="通知响应", description="通知响应")
public class OrderNoticeRepVO implements Serializable {
    private static final long serialVersionUID = -4812853711542457078L;

    @ApiModelProperty(value = "预约通知id")
    @TableId(value = "order_notice_id",type = IdType.ASSIGN_UUID)
    private String orderNoticeId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "通知类型id")
    private Integer noticeType;

    @ApiModelProperty(value = "内容类型id")
    private Integer contentType;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private OrderNoticeDTO content;

    @ApiModelProperty(value = "是否已读（0：未读 1：已读）")
    private Integer isRead;

    @ApiModelProperty(value = "推送时间")
    private Date sendTime;

}
