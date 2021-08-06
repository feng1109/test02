package com.eseasky.modules.space.service;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.vo.SpaceFloorVO;
import com.eseasky.modules.space.vo.response.DropDownVO;

/**
 * <p>
 * 楼层服务接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
public interface SpaceFloorService extends IService<SpaceFloor> {

    R<String> addFloor(JSONObject param);

    R<String> deleteFloor(String floorId);

    R<String> editFloor(JSONObject param);

    R<List<SpaceFloorVO>> findFloorList(String buildId);

    boolean createFloorByBuildId(Integer floorCount, String buildId, Date now);

    R<List<DropDownVO>> getFloorDropDown(String buildId);

    R<String> moveFloor(JSONObject param);

    R<String> modifyConfId(JSONObject param);

}
