package com.eseasky.modules.space.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.vo.excel.ExcelOneBOneF;
import com.eseasky.modules.space.vo.response.OneBOneF;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Repository
public interface SpaceFloorMapper extends BaseMapper<SpaceFloor> {

    List<SpaceFloor> findFloorList(@Param("buildId") String buildId, @Param("roomName") String roomName);

    OneBOneF getOneBuildOneFloor(@Param("floorId") String floorId);

    List<ExcelOneBOneF> getBuildAndFloorForExcel();

}
