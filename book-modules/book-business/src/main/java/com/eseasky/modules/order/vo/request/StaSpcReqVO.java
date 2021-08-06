package com.eseasky.modules.order.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @describe: 统计请求参数
 * @title: StaSpcReqVO
 * @Author lc
 * @Date: 2021/5/20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "StaSpcReqVO")
@ApiModel(value="请求查看空间统计数据", description="请求查看空间统计数据")
public class StaSpcReqVO {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "建筑id")
    private List<String> buildId= Lists.newArrayList();

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "预约类型(1.单人短租/拼团 2.单人长租 3.会议短租 4.会议长租)")
    @NotNull(message = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "开始日期")
    @NotNull(message = "请传入时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @ApiModelProperty(value = "结束日期")
    @NotNull(message = "请传入时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @ApiModelProperty(value = "排序类型(1.次数顺次 2.次数倒叙 3.时间顺序 4.时间倒序)")
    private Integer sortType;

    @Min(value = 1,message = "页码最小为1")
    @ApiModelProperty(value = "页码",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageNum;

    @Min(value = 1,message = "页数最小为1")
    @ApiModelProperty(value = "页数",required = true)
    @NotNull(message = "请传入正确的分页参数")
    private Integer pageSize;

}