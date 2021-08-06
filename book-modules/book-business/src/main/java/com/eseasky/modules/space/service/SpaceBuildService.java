package com.eseasky.modules.space.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.vo.SpaceBuildVO;
import com.eseasky.modules.space.vo.request.EditBuildStateBatchVO;
import com.eseasky.modules.space.vo.request.QueryBuildParam;
import com.eseasky.modules.space.vo.request.QueryMobileBuildParam;
import com.eseasky.modules.space.vo.request.QueryUseRuleVO;
import com.eseasky.modules.space.vo.response.BuildListVO;
import com.eseasky.modules.space.vo.response.BuildMapVO;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.QueryMobileBulidList;
import com.eseasky.modules.space.vo.response.SubAdvanceDayVO;

/**
 * <p>
 * 综合楼服务接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
public interface SpaceBuildService extends IService<SpaceBuild> {

    public R<String> addBuild(SpaceBuildVO spaceBuildVO);

    public R<String> deleteBuild(String buildId);

    public R<String> editBuild(SpaceBuildVO spaceBuildVO);

    public R<PageListVO<SpaceBuildVO>> getSpaceBuildList(QueryBuildParam param);

    public R<SpaceBuildVO> findOneBuild(String buildId);

    public R<BuildListVO<QueryMobileBulidList>> getMobileBuildList(QueryMobileBuildParam param);

    public R<SubAdvanceDayVO> getSubAdvanceDay();

    public void cancelOrder(List<String> idList, int spaceType);

    public R<String> deleteBuildBatch(JSONObject param);

    public R<String> editBuildStateBatch(EditBuildStateBatchVO param);

    public R<String> modifyConfId(JSONObject param);

    public R<List<BuildMapVO>> getBuildListForMap();

    public R<String> addUseRule(QueryUseRuleVO param);

    public R<QueryUseRuleVO> getUseRule(String buildId);

    public R<QueryUseRuleVO> getMobileUseRule(String buildId);

    public List<SpaceBuild> getAllBuildForMobileDistanceOrder(String tenantCode);

}
