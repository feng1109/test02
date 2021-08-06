package com.eseasky.modules.order.service;


import com.eseasky.modules.order.entity.OrderBlacklist;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
public interface OrderBlacklistService extends IService<OrderBlacklist> {

     Integer getIsInBlackList(String userId,String buildOrgId);

}
