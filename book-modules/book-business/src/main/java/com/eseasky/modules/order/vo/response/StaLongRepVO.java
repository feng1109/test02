package com.eseasky.modules.order.vo.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;
import java.util.List;

/**
 * @describe:
 * @title: StaShortRepVO
 * @Author lc
 * @Date: 2021/5/24
 */
@Data
@Alias("StaLongRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="响应长租订单详情", description="响应长租订单详情")
public class StaLongRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约座位订单id")
    private String orderSeatId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "人员类型")
    private String userType;

    @ApiModelProperty(value = "学号")
    private String userNo;

    @ApiModelProperty(value = "手机号")
    private String userPhone;

    @ApiModelProperty(value = "组织名称")
    private String orgName;

    @ApiModelProperty(value = "预约时间(前端展示)")
    private String showTime;

    @ApiModelProperty(value = "预约开始时间")
    private String orderStartTime;

    @ApiModelProperty(value = "预约结束时间")
    private String orderEndTime;

    @ApiModelProperty(value = "使用开始时间")
    private Date userStartTime;

    @ApiModelProperty(value = "使用结束时间")
    private Date userEndTime;

    @ApiModelProperty(value = "使用时长")
    private String useTime;

    @ApiModelProperty(value = "订单状态")
    private Integer listState;

    @ApiModelProperty(value = "阈值")
    private String longRequireTime;

    @ApiModelProperty(value = "建筑名称")
    private String buildName;

    @ApiModelProperty(value = "楼层编号")
    private Integer floorNum;

    @ApiModelProperty(value = "房间名称")
    private String roomName;

    @ApiModelProperty(value = "房间年编号")
    private String roomNum;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "备注")
    private String remark;


    @ApiModelProperty(value = "签到记录")
    List<StaLongDetailRepVO> records= Lists.newArrayList();
}