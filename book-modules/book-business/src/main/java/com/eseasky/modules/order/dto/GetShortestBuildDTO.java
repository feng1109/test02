package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

/**
 * @describe:
 * @title: GetShortestBuildDTO
 * @Author lc
 * @Date: 2021/7/26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "距离最近的楼层信息")
@Alias("GetShortestBuildDTO")
public class GetShortestBuildDTO {

    private String buildId;

    private String area;

    private String coordx;

    private String coordy;

}