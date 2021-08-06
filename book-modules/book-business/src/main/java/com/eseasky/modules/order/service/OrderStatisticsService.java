package com.eseasky.modules.order.service;


import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.vo.request.AnaRoomReqVO;
import com.eseasky.modules.order.vo.request.OrderListReqVO;
import com.eseasky.modules.order.vo.request.StaSpcReqVO;
import com.eseasky.modules.order.vo.request.StaUserReqVO;

import javax.servlet.http.HttpServletResponse;

public interface OrderStatisticsService {

    R showSpaceStatistics(StaSpcReqVO staSpcReqVO);

    R showSpaceAnalysis(AnaRoomReqVO anaRoomReqVO);

    R showUserStatistics(StaUserReqVO staUserReqVO);

    R showUserAnalysis(String userId);

    R showOrderList(OrderListReqVO orderListReqVO);

    R showShortDetail(String orderSeatId);

    R showLongDetail(String orderSeatId);

    R showGroupDetail(String orderGroupId);

    R getBuildAndRoomDropDown();

    void outOrderList(OrderListReqVO orderListReqVO, HttpServletResponse httpServletResponse);

}
