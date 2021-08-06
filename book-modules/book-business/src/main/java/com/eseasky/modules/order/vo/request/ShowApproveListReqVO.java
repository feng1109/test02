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
 * @title: ShowApproveListReqVO
 * @Author lc
 * @Date: 2021/6/11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ShowApproveListReqVO")
@ApiModel(value="请求查看审批列表", description="请求查看审批列表")
public class ShowApproveListReqVO {

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date starTime;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;

    @ApiModelProperty("审批状态")
    private Integer approveState;

    @ApiModelProperty("查询条件")
    private String queryCondition;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageSize;

}