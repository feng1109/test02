package com.eseasky.modules.space.vo.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "批量修改房间的状态", description = "")
public class EditRoomStateBatchVO {

    @NotEmpty(message = "请填写房间id")
    @ApiModelProperty(value = "房间id集合", required = true)
    private List<String> roomIdList = new ArrayList<String>();

    @NotNull(message = "请填写房间状态")
    @ApiModelProperty(value = "房间状态，0关闭 1开放", required = true)
    private Integer roomState;

}
