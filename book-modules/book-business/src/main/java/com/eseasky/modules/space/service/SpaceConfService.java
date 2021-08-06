package com.eseasky.modules.space.service;

import java.util.LinkedHashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.request.DeleteConfUsedParam;
import com.eseasky.modules.space.vo.request.QueryConfParam;
import com.eseasky.modules.space.vo.request.QueryConfUsedParam;
import com.eseasky.modules.space.vo.response.ConfOrderTypeVO;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.eseasky.modules.space.vo.response.QueryConfResult;
import com.eseasky.modules.space.vo.response.QueryConfUsedVO;

/**
 * <p>
 * 配置规则接口服务
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
public interface SpaceConfService extends IService<SpaceConf> {

    R<String> addConf(SpaceConfVO spaceConfVO);

    R<String> deleteConfBatch(JSONObject param);

    R<String> editConf(SpaceConfVO spaceConfVO);

    R<PageListVO<QueryConfResult>> findConfList(QueryConfParam param);

    R<SpaceConfVO> findOneConf(String confId);

    LinkedHashMap<String, SpaceConfVO> confIdAndSpaceConf(String tenantCode);

    R<ConfOrderTypeVO> getConfMenuList();

    R<PageListVO<QueryConfUsedVO>> findConfUsedList(QueryConfUsedParam param);

    R<String> deleteConfUsedList(List<DeleteConfUsedParam> param);

    R<JSONObject> judgeConf(Integer orderType, String sDate, String eDate, String startTime, String endTime);

    R<List<DropDownVO>> getConfDropDownByCurrentUser();


}
