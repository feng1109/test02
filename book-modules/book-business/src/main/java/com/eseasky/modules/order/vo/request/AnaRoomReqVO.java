package com.eseasky.modules.order.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @describe:
 * @title: AnaRoomReqVO
 * @Author lc
 * @Date: 2021/6/3
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Alias(value = "AnaRoomReqVO")
@ApiModel(value="请求空间分析", description="请求空间分析")
public class AnaRoomReqVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "空间id",required = true)
    @NotNull(message = "请传入房间id")
    private String roomId;

    @ApiModelProperty(value = "预约类型",required = true)
    @NotNull(message = "请传入预约类型")
    private Integer orderType;

    @ApiModelProperty(value = "开始日期")
    @NotNull(message = "请传入时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @ApiModelProperty(value = "结束日期")
    @NotNull(message = "请传入时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;


}