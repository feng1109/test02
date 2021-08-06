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
 * 课桌服务
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceDesk对象", description = "")
public class SpaceDesk extends Model<SpaceDesk> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "课桌id")
    @TableId(value = "desk_id", type = IdType.ASSIGN_UUID)
    private String deskId;

    @ApiModelProperty(value = "房间id")
    private String roomId;

    @ApiModelProperty(value = "楼层id")
    private String floorId;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "1座位;2课桌;3墙")
    private Integer type;

    @ApiModelProperty(value = "x轴坐标")
    private Integer x;

    @ApiModelProperty(value = "y轴坐标")
    private Integer y;
    
    @ApiModelProperty(value = "前端画布标识，后端只负责保存")
    private String webId;
    
    @ApiModelProperty(value = "画布状态，墙壁、课桌默认0不可用")
    private Integer canvasState;
    
    @ApiModelProperty(value = "课桌状态，默认1，此字段暂时用不到")
    private Integer deskState;

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
        return this.deskId;
    }

}
