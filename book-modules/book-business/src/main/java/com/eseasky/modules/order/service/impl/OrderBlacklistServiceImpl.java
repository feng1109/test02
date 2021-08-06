package com.eseasky.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.modules.order.entity.OrderBlacklist;
import com.eseasky.modules.order.mapper.OrderBlacklistMapper;
import com.eseasky.modules.order.service.OrderBlacklistService;
import org.springframework.stereotype.Service;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2021-06-09
 */
@Service
public class OrderBlacklistServiceImpl extends ServiceImpl<OrderBlacklistMapper, OrderBlacklist> implements OrderBlacklistService {


    /**
     * @description: 查询用户是否在该组织黑名单
     * @author: lc
     * @date: 2021/6/21 11:30
     * @params
     * @return
     */
    @Override
    public Integer getIsInBlackList(String userId, String buildOrgId) {
        LambdaQueryWrapper<OrderBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderBlacklist::getUserId,userId)
                .eq(OrderBlacklist::getBuildOrgId,buildOrgId);
        return count(queryWrapper);
    }
}
