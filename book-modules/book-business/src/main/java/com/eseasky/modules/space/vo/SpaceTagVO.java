package com.eseasky.modules.space.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 场馆楼层房间座位的标签
 * </p>
 *
 * @author
 * @since 2021-07-21
 */
@Data
@ApiModel(value = "SpaceTag对象", description = "")
public class SpaceTagVO {


    @ApiModelProperty(value = "主键")
    private String tagId;

    @ApiModelProperty(value = "场馆楼层房间座位对应的id")
    private String spaceId;

    @ApiModelProperty(value = "1场馆，2楼层，3房间，4座位")
    private Integer spaceType;

    @ApiModelProperty(value = "靠近门，0否 1是")
    private Integer door;

    @ApiModelProperty(value = "靠窗户，0否 1是")
    private Integer window;

    @ApiModelProperty(value = "有插座，0否 1是")
    private Integer socket;

    @ApiModelProperty(value = "有空调，0否 1是")
    private Integer air;

    @ApiModelProperty(value = "靠近卫生间，0否 1是")
    private Integer toilet;

}
