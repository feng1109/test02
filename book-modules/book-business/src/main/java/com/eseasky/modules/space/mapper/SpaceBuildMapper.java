package com.eseasky.modules.space.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.modules.order.dto.GetShortestBuildDTO;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.vo.response.QueryMobileBulidList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Repository
public interface SpaceBuildMapper extends BaseMapper<SpaceBuild> {

    /** 排除已删除数据，不考虑禁用状态 */
    List<Map<String, String>> getBuildAndRoomStatisticsDropDown(@Param("orgIdList") Collection<String> orgIdList);

    List<SpaceBuild> getManyBuildManyFloor(@Param("orgIdList") Collection<String> orgIdList);

    /** 查询手机端综合楼列表 */
    List<QueryMobileBulidList> queryMobileBulidList(Page<QueryMobileBulidList> page, @Param("buildIdList") Collection<String> buildIdList);

    /** 查询手机端综合楼列表，不需要排序、分页，根据距离、评分、常去排序的时候用到 */
    List<QueryMobileBulidList> queryMobileBulidListNoPage(@Param("buildIdList") Collection<String> buildIdList);

    /** 查询距离所给经纬度距离最短的建筑*/
    List<GetShortestBuildDTO> getShortestBuild(@Param("coordx") String coordx,@Param("coordy")String coordy);

}
