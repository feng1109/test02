package com.eseasky.modules.space.vo;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.eseasky.modules.space.vo.request.SaveSeatVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 画布对象，空间、座位、课桌信息
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "SpaceCanvas画布对象", description = "")
public class SpaceCanvasVO {

    @NotBlank(message = "请输入空间id")
    @ApiModelProperty(value = "空间id", required = true)
    private String roomId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号|门牌号")
    private String roomNum;

    @NotEmpty(message = "请输入画布集合")
    @ApiModelProperty(value = "画布集合：座位、课桌、墙...", required = true)
    private List<SaveSeatVO> canvases;

}
