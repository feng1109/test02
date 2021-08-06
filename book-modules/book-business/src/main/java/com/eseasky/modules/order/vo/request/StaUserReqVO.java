package com.eseasky.modules.order.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "StaUserReqVO")
@ApiModel(value="请求查看用户统计数据", description="请求查看用户统计数据")
public class StaUserReqVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "组织id")
    private String orgId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "排序类型(1.次数顺次 2.次数倒叙 3.时间顺序 4.时间倒序 5.取消顺序 6.取消倒叙 7.违约顺序 8.违约倒叙 9.黑名单顺序 10.黑名单倒叙)")
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
