package com.eseasky.modules.space.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 门禁对象
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SpaceDoor对象", description = "")
public class SpaceDoor extends Model<SpaceDoor> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "门禁id")
    @TableId(value = "door_id", type = IdType.ASSIGN_UUID)
    private String doorId;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String foorId;

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "所属区域名称")
    private String area;

    @ApiModelProperty(value = "门禁名称")
    private String doorName;

    @ApiModelProperty(value = "门禁编号")
    private String doorNum;

    @ApiModelProperty(value = "门禁ip")
    private String doorIp;

    @ApiModelProperty(value = "门禁状态")
    private Integer doorState;
    
    @ApiModelProperty(value = "所属部门id")
    private String deptId;

    @ApiModelProperty(value = "创建人id")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人id")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


    @Override
    protected Serializable pkVal() {
        return this.doorId;
    }

}
