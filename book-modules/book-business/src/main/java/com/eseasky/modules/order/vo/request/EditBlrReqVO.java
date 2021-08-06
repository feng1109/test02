package com.eseasky.modules.order.vo.request;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @describe:
 * @title: CreatBlrReqVO
 * @Author lc
 * @Date: 2021/6/10
 */
@Data
@Alias(value = "EditBlrReqVO")
@ApiModel(value="请求编辑黑名单", description="请求编辑黑名单")
public class EditBlrReqVO {

    @ApiModelProperty(value = "黑名单规则id",required = true)
    @NotNull(message = "请传入黑名单规则id")
    private String blacklistRuleId;

    @ApiModelProperty(value = "规则名称",required = true)
    @NotNull(message = "请传入标题")
    private String ruleName;

    @ApiModelProperty(value = "组织id",required = true)
    @NotNull(message = "请传入组织id")
    private String orgId;

    @ApiModelProperty(value = "组织id",required = true)
    @NotNull(message = "请传入组织名称")
    private String orgName;

    @ApiModelProperty(value = "黑名单详情",required = true)
    private List<BlacklistDetailReqVO> BlacklistDetailList= Lists.newArrayList();
}