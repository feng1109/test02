package com.eseasky.modules.order.vo.request;

import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * OrderNoticeReqVO
 * @Author: 王鹏滔
 * @Date: 2021/7/7 9:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "OrderNoticeReqVO")
@ApiModel(value="请求通知数据", description="请求通知数据")
public class OrderNoticeReqVO extends PageHelper implements Serializable {

    private static final long serialVersionUID = -1808339455867531433L;

}