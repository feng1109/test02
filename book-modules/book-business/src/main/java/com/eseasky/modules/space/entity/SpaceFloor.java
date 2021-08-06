package com.eseasky.modules.space.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 楼层对象
 * </p>
 *
 * @author
 * @since 2021-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceFloor对象", description = "")
public class SpaceFloor extends Model<SpaceFloor> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "楼层id")
    @TableId(value = "floor_id", type = IdType.ASSIGN_UUID)
    private String floorId;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "第几层")
    private Integer floorNum;

    @ApiModelProperty(value = "楼层名称")
    private String floorName;

    @ApiModelProperty(value = "楼层平面图")
    private String floorImage;

    @ApiModelProperty(value = "楼层状态：0禁用，1可用")
    private Integer floorState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "配置规则设置人id")
    private String confUser;

    @ApiModelProperty(value = "配置规则设置日期")
    private Date confTime;

    @ApiModelProperty(value = "创建人id")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人id")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除，0删除，1未删除")
    @TableLogic(value = "1", delval = "0")
    private Integer delFlag;

    @ApiModelProperty(value = "空间集合")
    @TableField(exist = false)
    private List<SpaceRoom> roomList;

    @Override
    protected Serializable pkVal() {
        return this.floorId;
    }

}
