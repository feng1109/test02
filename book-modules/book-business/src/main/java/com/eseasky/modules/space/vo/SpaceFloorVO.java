package com.eseasky.modules.space.vo;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 楼层对象
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "SpaceFloor楼层对象", description = "")
public class SpaceFloorVO {

    @ApiModelProperty(value = "楼层id，保存没有修改必传")
    private String floorId;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "第几层")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层平面图")
    private String floorImage;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "楼层状态")
    private Integer floorState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "配置规则设置人id")
    private String confUser;

    @ApiModelProperty(value = "配置规则设置日期")
    private Date confTime;

    @ApiModelProperty(value = "空间集合")
    private List<SpaceRoomVO> roomList;

}
