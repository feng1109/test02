package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: ShowBlrListReqVO
 * @Author lc
 * @Date: 2021/6/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ShowBlrListReqVO")
@ApiModel(value="请求查看黑名单请求", description="请求查看黑名单请求")
public class ShowBlrListReqVO {

    @ApiModelProperty(value = "组织id")
    private String orgId;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageSize;


}