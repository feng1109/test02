package com.eseasky.modules.order.vo.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * @describe: 查看短租订单列表分页（）
 * @title: ShortListPageReqVO
 * @Author lc
 * @Date: 2021/5/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Alias(value = "ShortListPageReqVO")
@ApiModel(value="请求长短租订单列表", description="请求长短短租订单列表")
public class ListPageRePVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总条数")
    private Integer total;

    @ApiModelProperty(value = "总页数")
    private Integer pages;

    private List<? extends Object> listVOS= Lists.newArrayList();

}