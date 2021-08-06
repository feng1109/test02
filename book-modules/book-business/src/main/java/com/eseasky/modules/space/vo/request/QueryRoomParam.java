package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.Min;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 空间列表查询参数
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "空间列表查询参数", description = "")
public class QueryRoomParam {

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "第几页，至少一页", required = true)
    private long pageNum;

    @Min(value = 1, message = "请填写分页参数")
    @ApiModelProperty(value = "每页数量，至少一条", required = true)
    private long pageSize;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

}
