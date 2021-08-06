package com.eseasky.modules.order.service;

import com.eseasky.modules.order.entity.OrderUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-04-15
 */
public interface OrderUserService extends IService<OrderUser> {

    Integer isUserExit(String userId);

    void addOrderCount(String  userId);

    void addCancelCount(String userId);

    void addLearnTime(String userId,Integer time);

}
