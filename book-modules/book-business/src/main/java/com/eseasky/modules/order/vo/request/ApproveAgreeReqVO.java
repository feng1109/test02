package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;

/**
 * @describe:
 * @title: ApproveAgreeReqVO
 * @Author lc
 * @Date: 2021/6/30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "ApproveAgreeReqVO")
@ApiModel(value="请求同意审批", description="请求同意审批")
public class ApproveAgreeReqVO {

    @ApiModelProperty(value = "审批id")
    @NotNull(message = "请传入审批id")
    private String approveId;


}