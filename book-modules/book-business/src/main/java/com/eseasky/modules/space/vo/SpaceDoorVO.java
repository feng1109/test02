package com.eseasky.modules.space.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 门禁对象
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
@Data
@ApiModel(value = "SpaceDoor门禁对象", description = "")
public class SpaceDoorVO {

    @ApiModelProperty(value = "门禁id，新增没有修改必传")
    private String doorId;

    @ApiModelProperty(value = "综合楼id", required = true)
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String foorId;

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "所属区域名称")
    private String area;

    @ApiModelProperty(value = "门禁名称")
    private String doorName;

    @ApiModelProperty(value = "门禁编号")
    private String doorNum;

    @ApiModelProperty(value = "门禁ip", required = true)
    private String doorIp;

    @ApiModelProperty(value = "门禁状态")
    private Integer doorState;

    @ApiModelProperty(value = "所属部门id", required = true)
    private String deptId;


    /** 不存在于实体类的其他属性 */
    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

}
