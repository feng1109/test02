package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "反馈信息", description = "反馈信息")
public class FeedBackRepVO implements Serializable {

    private static final long serialVersionUID = 2577827240451427938L;

    @ApiModelProperty(value = "反馈id")
    private String feedBackId;

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

    @ApiModelProperty(value = "预约人")
    private String userId;

    @ApiModelProperty(value = "预约人名")
    private String userName;

    @ApiModelProperty(value = "反馈类型")
    private Integer type;

    @ApiModelProperty(value = "状态")
    private Integer state;

    @ApiModelProperty(value = "反馈内容")
    private String content;

    @ApiModelProperty(value = "创建人")
    private String createUserId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "订单id")
    private String orderListId;

    @ApiModelProperty(value = "更新人")
    private String updateUserId;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "是否逻辑删除")
    private String delFlag;

}
