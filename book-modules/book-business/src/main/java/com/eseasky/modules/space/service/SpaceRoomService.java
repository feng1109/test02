package com.eseasky.modules.space.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.SpaceFloorVO;
import com.eseasky.modules.space.vo.SpaceRoomVO;
import com.eseasky.modules.space.vo.request.EditRoomStateBatchVO;
import com.eseasky.modules.space.vo.request.ImportRoomImageVO;
import com.eseasky.modules.space.vo.request.QueryMobileRoomParam;
import com.eseasky.modules.space.vo.request.QueryOrderRoomParam;
import com.eseasky.modules.space.vo.request.QueryRoomParam;
import com.eseasky.modules.space.vo.request.QueryStatisticsRoomParam;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.PageListVO;

/**
 * <p>
 * 空间服务接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
public interface SpaceRoomService extends IService<SpaceRoom> {

    R<String> addRoom(SpaceRoomVO spaceRoomVO);

    R<String> deleteRoom(String roomId);

    R<String> editRoom(SpaceRoomVO spaceRoomVO);

    R<String> deleteRoomBatch(JSONObject param);

    R<String> editRoomStateBatch(EditRoomStateBatchVO param);

    R<PageListVO<OneBOneFOneR>> getSpaceRoomList(QueryRoomParam param);

    boolean modifySeatCountAndDeskCount(String roomId, Integer seatCount, Integer seatNotForbidCount);

    R<SpaceRoomVO> findOneRoom(String roomId);

    R<String> modifyConfId(JSONObject roomId);

    R<List<SpaceFloorVO>> getMobileRoomList(QueryMobileRoomParam param);

    R<PageListVO<SpaceRoomVO>> getStatisticRoomList(QueryStatisticsRoomParam param);

    R<OneBOneFOneR> getOneRoom(String roomId);

    R<List<DropDownVO>> getStatisticDropDown();

    /** code=0返回OneBOneFOneR，code=1返回null */
    R<OneBOneFOneR> getRoomInfoForOrder(QueryOrderRoomParam param);

    /** code=0返回SpaceConf，code=1返回null */
    R<SpaceConfVO> getRoomConfForOrder(String roomId);

    R<String> importRoomImage(ImportRoomImageVO param);

}
