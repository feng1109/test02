package com.eseasky.modules.order.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2021-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="违约记录详情", description="违约记录详情")
public class OrderViolateDetailVO extends Model<OrderViolateDetailVO> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "违约记录id")
    private String violateListId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "违规时间")
    private Date violateTime;

    @ApiModelProperty(value = "黑名单规则违约类型")
    private Integer blacklistRuleType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private String delFlag;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
