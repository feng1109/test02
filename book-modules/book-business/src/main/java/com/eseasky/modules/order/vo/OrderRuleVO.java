package com.eseasky.modules.order.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
@ApiModel(value="预约规则", description="预约规则")
public class OrderRuleVO extends Model<OrderRuleVO> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约规则id")
    @NotNull(message = "id不能为空")
    private String ruleId;

    @ApiModelProperty(value = "预约：可提前天数")
    private Integer subAdvanceDay;

    @ApiModelProperty(value = "预约：今日预约几点截止")
    private String subTodayEndTime;

    @ApiModelProperty(value = "预约：取消多少次后当日不能进行预约")
    private Integer subCancelCount;

    @ApiModelProperty(value = "预约：每天预约次数")
    private Integer subLimitCount;

    @ApiModelProperty(value = "预约：当日允许预约最短时间(min)")
    private Integer subMinTime;

    @ApiModelProperty(value = "预约：当日允许预约最大时间(min)")
    private Integer subMaxTime;

    @ApiModelProperty(value = "预约：本次预约结束前多长时间内可续约(min)")
    private Integer subContinueTime;

    @ApiModelProperty(value = "预约：允许拼团时间(min)")
    private Integer groupLimitTime;;

    @ApiModelProperty(value = "签到：迟到多少时间不可签到(min)")
    private Integer signLateTime;

    @ApiModelProperty(value = "签到：提前多少时间可以签到(min)")
    private Integer signAdvanceTime;

    @ApiModelProperty(value = "签到：距签到前多长时间内不能取消预约(min)")
    private Integer signCancelLimitTime;

    @ApiModelProperty(value = "签到：签退限制时间（min）")
    private Integer signLeaveLimitTime;

    @ApiModelProperty(value = "签到：暂离状态超过多长时间不可恢复（min）")
    private Integer signAwayLimitTime;

    @ApiModelProperty(value = "签到：距离场馆坐标范围内多少距离可签到(km)")
    private Integer signMaxDistance;

    @ApiModelProperty(value = "是否可审批")
    private Integer isNeedApprove;

    @ApiModelProperty(value = "使用规则")
    private String useRule;

    @Override
    protected Serializable pkVal() {
        return this.ruleId;
    }

}
