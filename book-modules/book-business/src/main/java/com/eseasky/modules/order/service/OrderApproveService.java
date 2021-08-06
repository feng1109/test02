package com.eseasky.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.dto.InsertApproveInfoDTO;
import com.eseasky.modules.order.entity.OrderApprove;
import com.eseasky.modules.order.vo.request.ApproveAgreeReqVO;
import com.eseasky.modules.order.vo.request.ApproveRejectReqVO;
import com.eseasky.modules.order.vo.request.ShowApproveListReqVO;

/**
 * <p>
 * 审批 服务类
 * </p>
 *
 * @author 
 * @since 2021-06-11
 */
public interface OrderApproveService extends IService<OrderApprove> {

    /** 插入审批数据*/
    void insertApproveInfo(InsertApproveInfoDTO insertApproveInfoDTO);


    R showApproveList(ShowApproveListReqVO showApproveListReqVO);


    R agreeApproveList(ApproveAgreeReqVO ApproveAgreeReqVO);

    R rejectApproveList(ApproveRejectReqVO approveRejectReqVO);

}
