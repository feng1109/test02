package com.eseasky.modules.space.mapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.QueryConfResult;
import com.eseasky.modules.space.vo.response.QueryConfUsedVO;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
public interface SpaceConfMapper extends BaseMapper<SpaceConf> {

    List<SpaceConf> getManyConfManyProp();

    List<DropDownVO> getConfDropDown(@Param("orgIdList") Collection<String> orgIdList);

    List<QueryConfResult> findConfList( //
            Page<QueryConfResult> page, //
            @Param("orderType") Integer orderType, //
            @Param("confDeptId") String confDeptId, //
            @Param("confName") String confName, //
            @Param("orgIdList") Collection<String> orgIdList);

    List<QueryConfUsedVO> findConfUsedList( //
            Page<QueryConfUsedVO> page, //
            @Param("confId") String confId, //
            @Param("spaceName") String spaceName);

    List<QueryConfUsedVO> findConfInUsed();

}
