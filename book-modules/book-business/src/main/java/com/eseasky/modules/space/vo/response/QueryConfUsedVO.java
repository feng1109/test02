package com.eseasky.modules.space.vo.response;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则列查询结果
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "配置规则列查询结果", description = "")
public class QueryConfUsedVO {

    @ApiModelProperty(value = "单位id")
    private String spaceId;

    @ApiModelProperty(value = "单位名称")
    private String spaceName;

    @ApiModelProperty(value = "单位类型id")
    private Integer spaceType;

    @ApiModelProperty(value = "单位类型名称")
    private String spaceTypeName;

    @ApiModelProperty(value = "单位状态，0关闭，1开放")
    private Integer spaceState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "创建人姓名")
    private String confUser;

    @ApiModelProperty(value = "创建时间")
    private Date confTime;


    @ApiModelProperty(value = "综合楼id")
    private String buildId;

}
