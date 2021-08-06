package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 手机预约界面：空间选择列表
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "手机预约界面：空间选择列表", description = "")
public class QueryMobileRoomParam {

    @NotBlank(message = "请填写综合楼id")
    @ApiModelProperty(value = "综合楼id", required = true)
    private String buildId;

    @NotNull(message = "请填写预约类型")
    @ApiModelProperty(value = "预约类型", required = true)
    private Integer orderType;

    @NotBlank(message = "请填写预约开始日期")
    @ApiModelProperty(value = "预约日期，yyyy-MM-dd", required = true)
    private String startDate;

    @NotBlank(message = "请填写预约结束日期")
    @ApiModelProperty(value = "预约日期，yyyy-MM-dd", required = true)
    private String endDate;

    @NotBlank(message = "请填写预约开始时间")
    @ApiModelProperty(value = "预约开始时间，HH:mm", required = true)
    private String startTime;

    @NotBlank(message = "请填写预约结束时间")
    @ApiModelProperty(value = "预约开始时间，HH:mm", required = true)
    private String endTime;

    @ApiModelProperty(value = "空间名称")
    private String roomName;
    
    
    
    @ApiModelProperty(value = "常去（勾选是1，不选是0），单选")
    private Integer usedToGo = 0;

    @ApiModelProperty(value = "高评分（勾选是1，不选是0），单选")
    private Integer highScore = 0;

    @ApiModelProperty(value = "靠窗（勾选是1，不选是0），可多选")
    private Integer closeWindow = 0;

    @ApiModelProperty(value = "有电源（勾选是1，不选是0），可多选")
    private Integer haveSocket = 0;

    @ApiModelProperty(value = "不靠洗手间（勾选是1，不选是0），可多选")
    private Integer awayToilet = 0;

    @ApiModelProperty(value = "不靠门（勾选是1，不选是0），可多选")
    private Integer awayDoor = 0;

}
