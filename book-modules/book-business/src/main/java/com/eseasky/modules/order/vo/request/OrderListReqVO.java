package com.eseasky.modules.order.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @describe:
 * @title: OrderListVO
 * @Author lc
 * @Date: 2021/4/16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "OrderListReqVO")
@ApiModel(value="请求获取预约订单", description="请求获取预约订单")
public class OrderListReqVO {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "建筑id")
    private String buildId;

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    @ApiModelProperty(value = "订单状态")
    private Integer listState;

    @ApiModelProperty(value = "排序方式")
    private Integer sortType;


    @ApiModelProperty(value = "查询条件：学号/姓名")
    private String queryCondition;

    @ApiModelProperty(value = "订单类型",required = true)
    private Integer orderType;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageSize;

}