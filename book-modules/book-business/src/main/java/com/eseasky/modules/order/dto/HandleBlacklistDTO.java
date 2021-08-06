package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;


/**
 * @describe:
 * @title: handleBlacklistDTO
 * @Author lc
 * @Date: 2021/4/27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "处理黑名单信息")
@Alias("HandleBlacklistDTO")
public class HandleBlacklistDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "租户id")
    private String  tenantCode;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "建筑组织id")
    private String buildOrgId;

    @ApiModelProperty(value = "违约类型")
    private Integer violateType;

    @ApiModelProperty(value = "违规时间")
    private Date violateTime;

    @ApiModelProperty(value = "进入黑名单时间（单位天 ）/处理黑名单解除时加入")
    private Integer effectDay;
    /**
     * 仅和订单业务相关时才会有此数据
     */
    @ApiModelProperty(value = "订单类型")
    private Integer orderType;
}