package com.eseasky.modules.order.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe: 长租详情签到记录
 * @title: StaLongDetailRepVO
 * @Author lc
 * @Date: 2021/5/25
 */

@Data
@Alias("StaLongDetailRepVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="响应长租详情签到记录", description="响应长租详情签到记录")
public class StaLongDetailRepVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "签到时间")
    private String arriveTime;

    @ApiModelProperty(value = "签退时间")
    private String leaveTime;

}