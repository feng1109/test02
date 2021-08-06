package com.eseasky.modules.order.vo.response;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2021-04-15
 */
@Data
@Alias("OrderUserRePVO")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderUser对象", description="")
public class OrderUserRePVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预约人员id")
    private String orderUserId;

    @ApiModelProperty(value = "人员id")
    private String userId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "院系")
    private String[] orgName;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "学号")
    private String userNo;



}
