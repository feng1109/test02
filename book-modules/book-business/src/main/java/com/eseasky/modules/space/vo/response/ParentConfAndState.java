package com.eseasky.modules.space.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ParentConfAndState {

    @ApiModelProperty(value = "0禁用，1可用")
    private Integer seatState;

    @ApiModelProperty(value = "0无禁用，1综合楼禁用，2楼层禁用，3房间禁用，4座位禁用")
    private Integer parentState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "0无配置，1综合楼配置，2楼层配置，3房间配置，4座位配置")
    private Integer parentConf;

}
