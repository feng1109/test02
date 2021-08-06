package com.eseasky.modules.space.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.vo.SpaceGroupVO;
import com.eseasky.modules.space.vo.request.QueryGroupParam;
import com.eseasky.modules.space.vo.request.QueryMobileSeatParam;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.PageListVO;

/**
 * <p>
 * 座位分组 服务类
 * </p>
 *
 * @author
 * @since 2021-06-18
 */
public interface SpaceGroupService extends IService<SpaceGroup> {

    R<String> addGroup(SpaceGroupVO param);

    R<String> editGroup(SpaceGroupVO param);

    R<String> deleteGroup(String groupId);

    R<PageListVO<SpaceGroupVO>> getSpaceGroupList(QueryGroupParam param);

    Map<String, SpaceGroupVO> groupIdAndSpaceGroupVO(String roomId);

    R<List<DropDownVO>> getGroupConfDropDown();

    R<List<SpaceGroupVO>> getMobileGroupList(QueryMobileSeatParam param);

}
