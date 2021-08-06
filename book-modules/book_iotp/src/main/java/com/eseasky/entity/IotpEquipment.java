package com.eseasky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * @Author YINJUN
 * @Date 2021年08月05日 15:27
 * @Description:物联设备实体类
 */
@Data
@ApiModel(value = "物联设备实例",description = "物联设备实例")
public class IotpEquipment {
    @ApiModelProperty(value = "设备id")
    @TableId(value = "id",type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "综合楼id")
    private String buildId;

    @ApiModelProperty(value = "楼层id")
    private String foorId;

    @ApiModelProperty(value = "空间id")
    private String roomId;

    @ApiModelProperty(value = "所属区域名称")
    private String area;

    @ApiModelProperty(value = "物联设备名称")
    private String equipmentName;

    @ApiModelProperty(value = "应用id")
    private  Integer appId;

    @ApiModelProperty(value = "应用名称")
    private  String appName;

    @ApiModelProperty(value = "设备类型id")
    private  String typeId;

    @ApiModelProperty(value = "设备类型名称")
    private  String typeName;

    @ApiModelProperty(value = "物联网平台空间id")
    private  Integer spaceId;

    @ApiModelProperty(value = "逻辑删除：0表示未删除，1表示已删除")
    private  Integer isDelete;

    @ApiModelProperty(value = "创建人")
    private  String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改人")
    private String updateUser;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

}
