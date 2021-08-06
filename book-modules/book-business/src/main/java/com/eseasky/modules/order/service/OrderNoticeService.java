package com.eseasky.modules.order.service;

import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.dto.SendNoticeDTO;
import com.eseasky.modules.order.entity.OrderNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.modules.order.vo.request.OrderNoticeReqVO;
import com.eseasky.modules.order.vo.response.ListWithPage;
import com.eseasky.modules.order.vo.response.OrderNoticeRepVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-05-14
 */
public interface OrderNoticeService extends IService<OrderNotice> {

    void sendNotice(SendNoticeDTO sendNoticeDTO);

    R<ListWithPage<OrderNoticeRepVO>> showNoticeList(OrderNoticeReqVO orderNoticeReqVO);

    R readNotice(String orderNoticeId);

    R delNotice(String orderNoticeId);

    /**
     * 根据是否已读获取通知条数
     * @param isRead
     * @return
     */
    R<Integer> getNoticeCount(Integer isRead);
}
