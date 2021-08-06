package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.Min;

/**
 * @describe:
 * @title: RoomCommentReqVO
 * @Author lc
 * @Date: 2021/5/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "RoomCommentReqVO")
@ApiModel(value="请求查看空间评论", description="请求查看空间评论")
public class RoomCommentReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "评论id")
    private String orderCommentId;

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "排序类型")
    private Integer sortType;

    @Min(value = 1)
    @ApiModelProperty(value = "页码")
    private Integer pageNum;

    @Min(value = 1)
    @ApiModelProperty(value = "页数")
    private Integer pageSize;



}