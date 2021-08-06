package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 统计界面：空间展示列表
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "统计界面：空间展示列表", description = "")
public class QueryStatisticsRoomParam {

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "第几页，至少一页", required = true)
    private long pageNum;

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "每页数量，至少一条", required = true)
    private long pageSize;

    @NotBlank(message = "请填写场馆id")
    @ApiModelProperty(value = "场馆id，必填，默认用下拉框第一个", required = true)
    private String buildId;

    @ApiModelProperty(value = "楼层id，可以不填，查当前场馆的全部楼层")
    private String floorId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @Min(value = 1, message = "请填写类型")
    @ApiModelProperty(value = "1座位，3会议室，默认1", required = true)
    private int type;

}
