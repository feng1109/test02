package com.eseasky.modules.space.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.space.entity.SpaceDesk;
import com.eseasky.modules.space.vo.request.SaveSeatVO;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
public interface SpaceDeskMapper extends BaseMapper<SpaceDesk> {

    List<SaveSeatVO> findListByRoomId(@Param("roomId") String roomId);

}
