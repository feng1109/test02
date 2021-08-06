package com.eseasky.modules.space.vo.request;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 为房间导入空间平面图
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@ApiModel(value = "为房间导入空间平面图", description = "")
public class ImportRoomImageVO {

    @NotBlank(message = "请填写空间id")
    @ApiModelProperty(value = "空间id", required = true)
    private String roomId;

    @NotBlank(message = "请填写空间平面图")
    @ApiModelProperty(value = "空间平面图", required = true)
    private String roomImage;

}
