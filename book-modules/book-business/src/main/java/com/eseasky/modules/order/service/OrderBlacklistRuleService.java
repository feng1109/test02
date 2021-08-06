package com.eseasky.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.entity.OrderBlacklistRule;
import com.eseasky.modules.order.vo.request.CreateBlrReqVO;
import com.eseasky.modules.order.vo.request.EditBlrReqVO;
import com.eseasky.modules.order.vo.request.ShowBlrListReqVO;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
public interface OrderBlacklistRuleService extends IService<OrderBlacklistRule> {

    /** 创建黑名单*/
    R creatBlacklistRule(CreateBlrReqVO createBlrReqVO);

    R getAlreadyCreateOrgId();

    /** 查看黑名单详情*/
    R showBlrDetail(String ruleId);

    /** 编辑黑名单规则*/
    R editBlacklistRule(EditBlrReqVO editBlrReqVO);

    /** 查看黑名单规则列表*/
    R showBlrList(ShowBlrListReqVO showBlrListReqVO);

    /** 批量删除规则*/
    R delBlacklistRule(String[] ruluIds);
}
