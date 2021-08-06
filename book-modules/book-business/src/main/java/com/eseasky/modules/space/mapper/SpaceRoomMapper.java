package com.eseasky.modules.space.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Repository
public interface SpaceRoomMapper extends BaseMapper<SpaceRoom> {

    List<OneBOneFOneR> findRoomList(Page<OneBOneFOneR> page, //
            @Param("buildId") String buildId, //
            @Param("floorId") String floorId, //
            @Param("roomName") String roomName, //
            @Param("orgIdList") Collection<String> orgIdList);

    OneBOneFOneR getOneBuildOneFloorOneRoom(@Param("roomId") String roomId);

}
