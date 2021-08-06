package com.eseasky.modules.space.vo.response;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SubAdvanceDayVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约：可提前天数")
    private Integer subAdvanceDay;

    @ApiModelProperty(value = "预约：当日允许预约最短时间(min)")
    private Integer subMinTime;

    @ApiModelProperty(value = "预约：当日允许预约最大时间(min)")
    private Integer subMaxTime;

    @ApiModelProperty(value = "预约：可提前预约日期数组")
    private List<String> subAdvanceDayList;

}
