package com.eseasky.modules.space.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 空间对象
 * </p>
 *
 * @author
 * @since 2021-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceRoom对象", description = "")
public class SpaceRoom extends Model<SpaceRoom> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "空间id")
    @TableId(value = "room_id", type = IdType.ASSIGN_UUID)
    private String roomId;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "空间名称")
    private String roomName;

    @ApiModelProperty(value = "空间编号、门牌号")
    private String roomNum;

    @ApiModelProperty(value = "空间类型")
    private Integer roomType;

    @ApiModelProperty(value = "空间平面图")
    private String roomImage;

    @ApiModelProperty(value = "空间所属区域")
    private String area;

    @ApiModelProperty(value = "座位可用和禁用总数")
    private Integer seatCount;

    @ApiModelProperty(value = "座位非禁用总数")
    private Integer seatNotForbidCount;

    @ApiModelProperty(value = "空间归属部门id")
    private String roomDeptId;

    @ApiModelProperty(value = "空间状态：0禁用，1可用")
    private Integer roomState;

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

    @ApiModelProperty(value = "逻辑删除，1未删除，0删除")
    @TableLogic(value = "1", delval = "0")
    private Integer delFlag;

    @Override
    protected Serializable pkVal() {
        return this.roomId;
    }

}
