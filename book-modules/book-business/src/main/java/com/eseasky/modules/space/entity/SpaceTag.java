package com.eseasky.modules.space.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 场馆楼层房间座位的标签
 * </p>
 *
 * @author
 * @since 2021-07-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceTag对象", description = "")
public class SpaceTag extends Model<SpaceTag> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "tag_id", type = IdType.ASSIGN_UUID)
    private String tagId;

    @ApiModelProperty(value = "场馆id")
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "座位id")
    private String seatId;

    @ApiModelProperty(value = "靠近门，0否 1是")
    private Integer door;

    @ApiModelProperty(value = "靠窗户，0否 1是")
    private Integer window;

    @ApiModelProperty(value = "有插座，0否 1是")
    private Integer socket;

    @ApiModelProperty(value = "有空调，0否 1是")
    private Integer air;

    @ApiModelProperty(value = "靠近卫生间，0否 1是")
    private Integer toilet;


    @Override
    protected Serializable pkVal() {
        return this.tagId;
    }

}
