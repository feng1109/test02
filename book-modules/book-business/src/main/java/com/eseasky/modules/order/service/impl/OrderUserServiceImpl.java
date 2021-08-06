package com.eseasky.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.dto.OrgDTO;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.entity.OrderUser;
import com.eseasky.modules.order.mapper.OrderUserMapper;
import com.eseasky.modules.order.service.OrderUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2021-04-15
 */
@Service
@Transactional
public class OrderUserServiceImpl extends ServiceImpl<OrderUserMapper, OrderUser> implements OrderUserService {

    @Autowired
    OrderUserMapper orderUserMapper;

    /**
     * @description:  获取用户表是否有用户信息
     * @author: lc
     * @date: 2021/6/8 16:02
     * @params
     * @return
     */
    @Override
    public Integer isUserExit(String userId) {

        // 若预约用户表没有该用户，创建用户信息
        LambdaQueryWrapper<OrderUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.select(OrderUser::getOrderUserId).eq(OrderUser::getUserId, userId);
        Integer isUserExit = orderUserMapper.selectCount(userQueryWrapper);
        if (isUserExit == 0) {
            // 初始化用户信息
            OrderUser orderUser = new OrderUser().setUserId(userId).setLearnMaxTime(0).setWeekLearnTime(0).setLearnTotalTime(0)
                    .setInBlacklistCount(0).setDelFlag("0").setOrderCount(0);

            // 插入用户组织信息
            SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
            List<OrgDTO> sysOrgs = sysUserDTO.getSysOrgs();
            String orgId = sysOrgs.stream().map(OrgDTO::getId).collect(Collectors.joining("/"));
            orderUser.setOrgId(orgId);

            orderUserMapper.insert(orderUser);
        }
        return isUserExit;
    }

    /**
     * @description: 用户预约次数加一
     * @author: lc
     * @date: 2021/7/29 17:58
     * @params
     * @return
     */
    @Override
    public void addOrderCount(String userId) {
        LambdaQueryWrapper<OrderUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OrderUser::getOrderCount).eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(wrapper);
        LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(OrderUser::getOrderCount, orderUser.getOrderCount() + 1)
                .eq(OrderUser::getUserId, userId);
        orderUserMapper.update(null, userLambdaUpdateWrapper);
    }

    /**
     * @description: 取消次数加一
     * @author: lc
     * @date: 2021/7/30 10:20
     * @params
     * @return
     */
    @Override
    public void addCancelCount(String userId) {
        LambdaQueryWrapper<OrderUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OrderUser::getCancelCount).eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(wrapper);
        LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(OrderUser::getCancelCount, orderUser.getCancelCount() + 1)
                .eq(OrderUser::getUserId, userId);
        orderUserMapper.update(null, userLambdaUpdateWrapper);
    }

    /**
     * @description: 增加学习时长
     * @author: lc
     * @date: 2021/7/30 10:30
     * @params
     * @return
     */
    @Override
    public void addLearnTime(String userId, Integer time) {
        LambdaQueryWrapper<OrderUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OrderUser::getLearnTotalTime).eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(wrapper);
        LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(OrderUser::getLearnTotalTime, orderUser.getLearnTotalTime() + time)
                .eq(OrderUser::getUserId, userId);
        orderUserMapper.update(null, userLambdaUpdateWrapper);
    }


}



