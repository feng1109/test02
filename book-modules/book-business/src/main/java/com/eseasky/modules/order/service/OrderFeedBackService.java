package com.eseasky.modules.order.service;

import com.alibaba.fastjson.JSONObject;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.entity.OrderFeedBack;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.modules.order.vo.request.OrderFeedBackAddReqVO;
import com.eseasky.modules.order.vo.request.OrderFeedBackHandReqVO;
import com.eseasky.modules.order.vo.request.OrderFeedBackPageReqVO;
import com.eseasky.modules.order.vo.response.FeedBackRepVO;
import com.eseasky.modules.order.vo.response.ListWithPage;

import java.util.List;

/**
 * <p>
 * 用户反馈 服务类
 * </p>
 *
 * @author
 * @since 2021-07-16
 */
public interface OrderFeedBackService extends IService<OrderFeedBack> {

    /**
     * 添加反馈
     * @param orderFeedBackAddReqVO
     * @return
     */
    R<String> addOrderFreeBack(OrderFeedBackAddReqVO orderFeedBackAddReqVO);

    /**
     * 分页查询反馈
     * @param orderFeedBackPageReqVO
     * @return
     */
    R<ListWithPage<FeedBackRepVO>> showFeedBackPage(OrderFeedBackPageReqVO orderFeedBackPageReqVO);

    /**
     * 批量处理反馈
     * @param orderFeedBackHandReqVO
     * @return
     */
    R<String> batchHandleFeedBack(OrderFeedBackHandReqVO orderFeedBackHandReqVO);

    /**
     * 获取场馆下拉框
     * @return
     */
    R<List<JSONObject>> getBuildNameDropDownBox();
}
