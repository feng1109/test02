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
@ApiModel(value="违约人员", description="违约人员")
public class OrderViolateUserVO extends Model<OrderViolateUserVO> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "违约用户id")
    private String violateUserId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "违规次数")
    private Integer violateCount;

    @ApiModelProperty(value = "是否在黑名单（0:不在 1:在）")
    private Integer isInBlacklist;

    @ApiModelProperty(value = "黑名单开始时间")
    private Date blacklistStartTime;

    @ApiModelProperty(value = "黑名单结束时间")
    private Date blacklistEndTime;

    @ApiModelProperty(value = "进入黑名单次数")
    private Integer inBlacklistCount;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.violateUserId;
    }

}
