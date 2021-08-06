package com.eseasky.modules.space.vo.response;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 手机端座位查询列表
 * </p>
 *
 * @author
 * @param <T>
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "手机端座位查询列表", description = "")
public class QueryMobileSeatListVO<T> {

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "综合楼编号")
    private String buildNum;

    @ApiModelProperty(value = "第几层")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号、门牌号")
    private String roomNum;

    @ApiModelProperty(value = "List集合")
    private List<T> list;

    @ApiModelProperty(value = "预约日期")
    private String orderDate;

    @ApiModelProperty(value = "预约开始时间")
    private String startTime;

    @ApiModelProperty(value = "预约结束时间")
    private String endTime;

    @ApiModelProperty(value = "座位总数")
    private Integer seatCount;

    @ApiModelProperty(value = "座位可用总数")
    private Integer seatNotUsedCount;

    @ApiModelProperty(value = "长租开始日期,yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @ApiModelProperty(value = "长租结束日期,yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
}
