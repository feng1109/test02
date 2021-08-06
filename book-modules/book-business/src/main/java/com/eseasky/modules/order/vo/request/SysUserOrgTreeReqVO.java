package com.eseasky.modules.order.vo.request;


import com.eseasky.modules.order.vo.response.PageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "SysUserOrgTreeReqVO")
@ApiModel(value = "查看组织架构请求", description = "查看组织架构请求")
public class SysUserOrgTreeReqVO implements Serializable {
    private static final long serialVersionUID = -6628214119945906068L;

    @ApiModelProperty("是否是用户 true：用户 false：组织")
    private Boolean user;

    @ApiModelProperty("组织id")
    private String id;

    @ApiModelProperty("页数")
    private Integer pageNum;

    @ApiModelProperty("条数")
    private Integer pageSize;
}
