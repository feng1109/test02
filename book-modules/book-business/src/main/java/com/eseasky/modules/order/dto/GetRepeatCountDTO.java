package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe:
 * @title: GetRepeatCountDTO
 * @Author lc
 * @Date: 2021/6/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "获取用户冲突订单数据")
@Alias("GetRepeatCountDTO")
public class GetRepeatCountDTO {


   private String userId;

   private Date orderStartTime;

   private Date orderEndTime;

}