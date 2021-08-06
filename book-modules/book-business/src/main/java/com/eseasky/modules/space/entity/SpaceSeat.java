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
 * 座位服务
 * </p>
 *
 * @author
 * @since 2021-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceSeat对象", description = "")
public class SpaceSeat extends Model<SpaceSeat> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "座位id")
    @TableId(value = "seat_id", type = IdType.ASSIGN_UUID)
    private String seatId;

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

    @ApiModelProperty(value = "座位编号")
    private String seatNum;

    @ApiModelProperty(value = "座位分组id")
    private String seatGroupId;

    @ApiModelProperty(value = "0禁用，1可用")
    private Integer seatState;

    @ApiModelProperty(value = "0无禁用，1综合楼禁用，2楼层禁用，3房间禁用，4座位禁用")
    private Integer parentState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "0无配置，1综合楼配置，2楼层配置，3房间配置，4座位配置")
    private Integer parentConf;

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
        return this.seatId;
    }

}
