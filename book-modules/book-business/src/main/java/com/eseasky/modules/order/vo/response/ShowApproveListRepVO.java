package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe:
 * @title: ShowApproveListReqVO
 * @Author lc
 * @Date: 2021/6/11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ShowApproveListRepVO")
@ApiModel(value="请求查看审批列表", description="请求查看审批列表")
public class ShowApproveListRepVO {

    @ApiModelProperty(value = "审批id")
    private String approveId;

    @ApiModelProperty(value = "订单id")
    private String orderListId;

    @ApiModelProperty(value = "订单类型 （5种：见字典表）")
    private Integer orderType;

    @ApiModelProperty(value = "申请人id")
    private String userId;

    @ApiModelProperty(value = "申请人姓名")
    private String userName;

    @ApiModelProperty(value = "申请时间")
    private Date applyTime;

    @ApiModelProperty(value = "预约开始时间")
    private Date orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private Date orderEndTime;

    @ApiModelProperty(value = "申请原因")
    private String reason;

    @ApiModelProperty(value = "地点")
    private String area;

    @ApiModelProperty(value = "审批状态 (1.待审批 2.通过 3.未通过 4.)")
    private Integer approveState;


}