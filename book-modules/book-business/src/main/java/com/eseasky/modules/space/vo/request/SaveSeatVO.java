package com.eseasky.modules.space.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 保存画布列表对象
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "保存画布列表对象", description = "")
public class SaveSeatVO {

    @ApiModelProperty(value = "座位id，保存没有修改必传")
    private String seatId;

    @ApiModelProperty(value = "1座位;2课桌;3墙", required = true)
    private Integer type;

    @ApiModelProperty(value = "0画布不可用状态;1空闲未预约状态;2预约使用中;3预约暂时中;4座位自身已禁用;5删除座位")
    private Integer state;

    @JsonIgnore
    @ApiModelProperty(value = "0无禁用，1综合楼禁用，2楼层禁用，3房间禁用，4座位禁用")
    private Integer parentState;

    @ApiModelProperty(value = "x轴坐标", required = true)
    private Integer x;

    @ApiModelProperty(value = "y轴坐标", required = true)
    private Integer y;

    @ApiModelProperty(value = "前端画布标识，后端只负责保存")
    private String webId;

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "座位配置规则id")
    private String confId;

    @ApiModelProperty(value = "座位分组id")
    private String seatGroupId;

}
