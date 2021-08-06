package com.eseasky.modules.space.vo.response;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 配置规则列表结果
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "配置规则列表结果", description = "")
public class QueryConfResult {

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "配置规则名称")
    private String confName;

    /** 配置表是数字，字典表是字符串，但是前端严格区分数字和字符串，只好这里改为字符串 */
    @ApiModelProperty(value = "预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "一次性预约|长租")
    private String orderTypeName;
    
    @ApiModelProperty(value = "内置规则不可修改，0否，1是")
    private Integer builtIn;

    @ApiModelProperty(value = "创建人姓名")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "正在使用空间数，默认0")
    private Integer inUsedCount = 0;


    @ApiModelProperty(value = "配置规则归属部门id")
    private String confDeptId;

    @ApiModelProperty(value = "配置规则归属部门名称")
    private String confDeptName;

}
