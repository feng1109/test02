package com.eseasky.modules.space.vo;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 空间对象
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "SpaceRoom空间对象", description = "")
public class SpaceRoomVO {

    @ApiModelProperty(value = "空间id，保存没有修改必传")
    private String roomId;

    @NotBlank(message = "请填写楼层id")
    @ApiModelProperty(value = "楼层id", required = true)
    private String floorId;

    @NotBlank(message = "请填写综合楼id")
    @ApiModelProperty(value = "综合楼id", required = true)
    private String buildId;

    @NotBlank(message = "请填写空间名称")
    @ApiModelProperty(value = "空间名称", required = true)
    private String roomName;

    @NotBlank(message = "请填写空间编号|门牌号")
    @ApiModelProperty(value = "空间编号|门牌号, required = true")
    private String roomNum;

    @ApiModelProperty(value = "空间类型")
    private Integer roomType;

    @ApiModelProperty(value = "空间平面图")
    private String roomImage;

    @ApiModelProperty(value = "空间所属区域")
    private String area;

    @NotNull(message = "请填写空间状态")
    @ApiModelProperty(value = "空间状态", required = true)
    private Integer roomState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "配置规则设置人id")
    private String confUser;

    @ApiModelProperty(value = "配置规则设置日期")
    private Date confTime;


    @ApiModelProperty(value = "座位总数")
    private Integer seatCount = 0;

    @ApiModelProperty(value = "座位已使用总数")
    private Integer seatInUsedCount = 0;

    @ApiModelProperty(value = "座位可使用总数")
    private Integer seatNotUsedCount = 0;


    @ApiModelProperty(value = "统计中心：会议室在当前时间点是否在使用中，0未使用，1使用中")
    private Integer isMeetingRoomInUsed = 0;


    @ApiModelProperty(value = "勾选‘高评分’的时候在房间上展示此房间的平均评分")
    private Double commentLevel = 0d;

}
