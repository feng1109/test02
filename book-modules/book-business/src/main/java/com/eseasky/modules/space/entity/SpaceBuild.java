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
 * 综合楼对象
 * </p>
 *
 * @author
 * @since 2021-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceBuild对象", description = "")
public class SpaceBuild extends Model<SpaceBuild> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "综合楼id")
    @TableId(value = "build_id", type = IdType.ASSIGN_UUID)
    private String buildId;

    @ApiModelProperty(value = "综合楼名称")
    private String buildName;

    @ApiModelProperty(value = "综合楼编号")
    private String buildNum;

    @ApiModelProperty(value = "楼层数量")
    private Integer floorCount;

    @ApiModelProperty(value = "综合楼X坐标（经度）")
    private String coordx;

    @ApiModelProperty(value = "综合楼Y坐标（纬度）")
    private String coordy;

    @ApiModelProperty(value = "综合楼位置名称")
    private String coordName;

    @ApiModelProperty(value = "综合楼图片")
    private String buildImage;

    @ApiModelProperty(value = "综合楼归属部门id")
    private String buildDeptId;

    @ApiModelProperty(value = "综合楼状态：0禁用，1可用")
    private Integer buildState;

    @ApiModelProperty(value = "配置规则id")
    private String confId;

    @ApiModelProperty(value = "配置规则设置人id")
    private String confUser;

    @ApiModelProperty(value = "配置规则设置日期")
    private Date confTime;

    @ApiModelProperty(value = "场馆使用规则")
    private String useRule;

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

    @ApiModelProperty(value = "楼层集合")
    @TableField(exist = false)
    private List<SpaceFloor> floorList;

    @Override
    protected Serializable pkVal() {
        return this.buildId;
    }

}
