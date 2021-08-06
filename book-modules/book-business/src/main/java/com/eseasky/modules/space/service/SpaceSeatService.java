package com.eseasky.modules.space.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.vo.OrderRuleVO;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.vo.SpaceCanvasVO;
import com.eseasky.modules.space.vo.request.ConfirmOrderParam;
import com.eseasky.modules.space.vo.request.QueryMobileSeatParam;
import com.eseasky.modules.space.vo.request.QueryOrderSeatParam;
import com.eseasky.modules.space.vo.request.QuickOrderParam;
import com.eseasky.modules.space.vo.request.SaveSeatVO;
import com.eseasky.modules.space.vo.response.ConfirmOrderVO;
import com.eseasky.modules.space.vo.response.QueryMobileSeatListVO;
import com.eseasky.modules.space.vo.response.SeatInfoToOrder;
import com.eseasky.modules.space.vo.response.SeatAndGroupForOrder;
import com.eseasky.modules.space.vo.response.SeatInfoForQuickOrder;

/**
 * <p>
 * 座位服务类
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
public interface SpaceSeatService extends IService<SpaceSeat> {

    public R<String> addOrUpdateSeat(SpaceCanvasVO canvasVO);

    public R<List<SaveSeatVO>> getSpaceSeatList(JSONObject param);

    public R<QueryMobileSeatListVO<SaveSeatVO>> getStatisticSeatList(JSONObject param);

    public R<QueryMobileSeatListVO<SaveSeatVO>> getMobileSeatList(QueryMobileSeatParam param);

    public R<SeatInfoForQuickOrder> mobileQuickOrder(QuickOrderParam param);

    public R<String> modifyConfId(JSONObject param);

    /** code=0返回SeatInfoForOrder，code=1返回错误信息Stirng */
    public R<SeatInfoToOrder> getSeatInfoToOrder(QueryOrderSeatParam param);

    /** 先判断座位seatId，再判断seatGroupId */
    public R<SeatAndGroupForOrder> getSeatAndGroupForOrder(String seatId, String seatGroupId);

    /** 根据座位id获取预约规则和签到规则 */
    public R<OrderRuleVO> getOrderRule(String seatId, String seatGroupId);

    /** 手机预约界面：确认订单时查询座位或房间详情 */
    public R<ConfirmOrderVO> getInfoToConfirmOrder(ConfirmOrderParam param);

}
