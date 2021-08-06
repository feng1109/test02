package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @describe:空间信息
 * @title: SpaceInfoDTO
 * @Author lc
 * @Date: 2021/4/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "空间信息")
public class SpaceInfoDTO {

    private static final long serialVersionUID = 1L;

    private String buildId;

    private String buildNum;

    private String  buildName;

    private String floorId;

    private Integer floorNum;

    private String floorName;

    private String roomId;

    private String roomNum;

    private String roomName;

    private String seatId;

    private String seatNum;

    @ApiModelProperty(value = "打卡方式 1.位置打卡  2.扫码打卡")
    private Integer clockType;


}