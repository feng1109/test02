package com.eseasky.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.entity.OrderComment;
import com.eseasky.modules.order.vo.request.ListCommentReqVO;
import com.eseasky.modules.order.vo.request.RoomCommentReqVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-05-10
 */
public interface OrderCommentService extends IService<OrderComment> {

    R commentList(ListCommentReqVO listCommentReqVO);

    R showListComment(RoomCommentReqVO roomCommentReqVO);

    R delListComment(String  orderCommentId);

    void delCommentBySpace(List<String> ids,Integer type);

}
