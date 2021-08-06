package com.eseasky.modules.space.vo.response;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * <p>
 * 综合楼列表查询结果
 * </p>
 *
 * @author
 * @param <T>
 * @since 2021-03-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "综合楼列表查询结果", description = "综合楼列表查询结果")
public class BuildListVO<T> {

    @ApiModelProperty(value = "数据总数")
    private long total;

    @ApiModelProperty(value = "总页数")
    private long pages;

    @ApiModelProperty(value = "List集合")
    private List<T> list;

    @ApiModelProperty(value = "当前页")
    private long pageNum;

    @ApiModelProperty(value = "每页数量")
    private long pageSize;

    @ApiModelProperty(value = "所有综合楼座位总数")
    private long buildSeatTotal;

    @ApiModelProperty(value = "所有综合楼座位使用总数")
    private long buildSeatInUsedTotal;

}
