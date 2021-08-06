package com.eseasky.modules.space.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.entity.SpaceDoor;
import com.eseasky.modules.space.vo.SpaceDoorVO;
import com.eseasky.modules.space.vo.request.QueryDoorParam;
import com.eseasky.modules.space.vo.response.PageListVO;

/**
 * <p>
 * 门禁服务接口
 * </p>
 *
 * @author
 * @since 2021-04-14
 */
public interface SpaceDoorService extends IService<SpaceDoor> {

    R<String> addDoor(SpaceDoorVO spaceDoorVO);

    R<String> deleteDoor(String doorId);

    R<String> editDoor(SpaceDoorVO spaceDoorVO);

    R<PageListVO<SpaceDoorVO>> findDoorList(QueryDoorParam param);

    R<SpaceDoorVO> findOneDoor(String doorId);

}
