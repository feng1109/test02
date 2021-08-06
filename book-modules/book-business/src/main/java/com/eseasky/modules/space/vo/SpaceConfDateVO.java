package com.eseasky.modules.space.vo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 可预约日期
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Data
@ApiModel(value = "SpaceConfDate对象", description = "")
public class SpaceConfDateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "可预约开始日期，yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date allowStartDate;

    @ApiModelProperty(value = "可预约结束日期，yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date allowEndDate;

}
