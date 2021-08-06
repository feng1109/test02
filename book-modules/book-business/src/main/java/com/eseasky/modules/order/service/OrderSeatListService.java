package com.eseasky.modules.order.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.dto.OrderListInfoDTO;
import com.eseasky.modules.order.dto.SpaceInfoDTO;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.vo.OrderSeatListVO;
import com.eseasky.modules.order.vo.request.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author
 * @since 2021-04-12
 */
public interface OrderSeatListService extends IService<OrderSeatList> {

    R test();

    List<Map<String, Object>> getUsedSeat(OrderListInfoDTO orderListInfoDTO);

    SpaceInfoDTO getAreaInfo(SpaceInfoDTO spaceInfoDTO);

    R showPCStatisticsData(StatisticsDataReqVO statisticsDataReqVO);

    R showLastTimeList(String UserId);

    R showListDetail(String userId);

    R userShortRentList(UserListReqVO userListReqVO);

    R userLongRentList(UserListReqVO userListReqVO);

    R showLongRentDetail(LongDetailReqVO longDetailReqVO);

    R showOftenUseArea(ShowOftenUseAreaReqVO showOftenUseAreaReqVO);

    R showUserInfo();

    R showStatistics(String month);

    R showClockInfo(ShowClockReqVO showClockReqVO);

    R creatOrderList(OrderSeatListVO orderSeatListVO, SysUserDTO sysUserDTO);

    R arriveOrderList(ArriveOrderReqVO arriveOrderReqVO);

    R awayOrderList(AwayOrderReqVO awayOrderReqVO);

    R backOrderList(AwayOrderReqVO awayOrderReqVO);

    R cancelOrderList(CancelOrderReqVO cancelOrderReqVO);

    R continueOrderList(ContinueOrderReqVO continueOrderReqVO);

    R leaveOrderList(LeaveOrderReqVO leaveOrderReqVO);

    void manageCancel(List<String> list,Integer idType);


    /******************************************************************************/

    /**
     * 座位id：seatId 订单状态：listState。 只有三种状态：1待签到;2使用中;3暂离
     */
    JSONObject getOrderedSeatByRoomId(Map<String, Object> param);



}
