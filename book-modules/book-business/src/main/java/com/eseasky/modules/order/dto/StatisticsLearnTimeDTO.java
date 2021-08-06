package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe: 统计每天学习时长
 * @title: statisticsLearnTimeDTO
 * @Author lc
 * @Date: 2021/5/7
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Alias(value = "statisticsLearnTimeDTO")
@ApiModel(value = "空间信息")
public class StatisticsLearnTimeDTO {

    private static final long serialVersionUID = 1L;

    private Integer Day;

    private Integer learnTime;

    private Integer count;

    private String userId;

    private String userName;

    private String roomId;

    private String roomName;




}