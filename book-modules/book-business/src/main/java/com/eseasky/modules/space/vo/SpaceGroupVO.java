package com.eseasky.modules.space.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.eseasky.modules.space.vo.request.SeatIdAndSeatNumVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 座位分组
 * </p>
 *
 * @author
 * @since 2021-06-18
 */
@Data
@ApiModel(value = "SpaceGroup对象", description = "")
public class SpaceGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键，新增没有修改必填")
    private String groupId;

    @NotBlank(message = "请填写组名称")
    @ApiModelProperty(value = "组名称", required = true)
    private String groupName;

    @NotBlank(message = "请填写空间id")
    @ApiModelProperty(value = "空间id", required = true)
    private String roomId;

    @NotBlank(message = "请填写配置规则")
    @ApiModelProperty(value = "配置规则id", required = true)
    private String confId;

    @ApiModelProperty(value = "座位数组，用于新增修改，座位组至少需要两张座位")
    List<SeatIdAndSeatNumVO> seatList = new ArrayList<>();

    @ApiModelProperty(value = "座位编号数组，仅用于展示")
    List<String> seatNumList = new ArrayList<>();

}
