package com.eseasky.modules.order.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.common.rabbitmq.dto.OrderMQDTO;
import com.eseasky.common.rabbitmq.message.RabbitMqUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderApprove;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.mapper.OrderApproveMapper;
import com.eseasky.modules.order.mapper.OrderGroupDetailMapper;
import com.eseasky.modules.order.mapper.OrderGroupListMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.service.OrderLockService;
import com.eseasky.modules.order.vo.OrderRuleVO;
import com.eseasky.modules.order.vo.request.GroupJoinReqVO;
import com.eseasky.modules.space.service.impl.SpaceSeatServiceImpl;
import com.google.common.collect.Maps;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;

/**
 * @describe: 该服务类专门处理加锁的方法
 * @title: OrderSeatLockServiceImpl
 * @Author lc
 * @Date: 2021/5/12
 */

@Service
public class OrderLockServiceImpl implements OrderLockService {


    @Autowired
    Redisson redisson;

    @Autowired
    OrderGroupDetailMapper orderGroupDetailMapper;

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    OrderGroupListMapper orderGroupListMapper;

    @Autowired
    OrderApproveMapper orderApproveMapper;

    @Autowired
    SpaceSeatServiceImpl spaceSeatService;

    @Autowired
    OrderSeatListServiceImpl orderSeatListService;

    @Autowired
    RabbitMqUtils rabbitMqUtils;



    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 对生成订单过程加分布式锁，生成订单前，去数据库查询是否有冲突的订单，若没有则生成订单
     * @author:
     * @date: 2021/5/4 22:39
     * @params [orderSeatList, tenantCode]
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R lockCreatList(OrderSeatList orderSeatList, String tenantCode) {

        String lockKey = tenantCode + ":order:creat-list-key-lock:" + orderSeatList.getSeatId();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            if (orderSeatListMapper.getSeatRepeat(orderSeatList) > 0) {
                throw BusinessException.of(5001, "该座位已被预约，请重新选座");
            }
            orderSeatListMapper.insert(orderSeatList);
        } finally {
            lock.unlock();
        }
        return R.ok("订单生成成功");
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 拼团预约：对生成订单过程加分布式锁，生成订单前，去数据库查询是否有冲突的订单，若没有则生成订单
     * @author:
     * @date: 2021/5/4 22:39
     * @params [orderSeatList, tenantCode]
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R lockCreatGroupList(OrderGroupList orderGroupList, String tenantCode) {

        String lockKey = tenantCode + ":order:creat-list-key-lock:" + orderGroupList.getSeatGroupId();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            if (orderGroupListMapper.getSeatGroupRepeat(orderGroupList) > 0) {
                throw BusinessException.of(5001, "该座位组已被预约，请重新选座");
            }
            orderGroupListMapper.insert(orderGroupList);
        } finally {
            lock.unlock();
        }
        return R.ok("订单生成成功");
    }

    /**
     * @return void
     * @description: 单人短租续约
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R shortContinue(String tenantCode, String orderSeatId, Date orderEndTime, String seatId) {

        // 生成分布式锁key（和预约订单共用一把锁）
        String lockKey = tenantCode + ":order:creat-list-key-lock:" + seatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        OrderSeatList getIsRepeat = new OrderSeatList();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            // 查询订单状态,续约次数,和订单结束时间,座位id
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getContinueCount, OrderSeatList::getOrderEndTime, OrderSeatList::getSeatId)
                    .eq(OrderSeatList::getOrderSeatId, orderSeatId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            Integer listState = orderSeatList.getListState();
            Integer continueCount = orderSeatList.getContinueCount();

            // 查看该该座位是否已被占用
            getIsRepeat.setSeatId(seatId)
                    .setOrderStartTime(orderSeatList.getOrderEndTime())
                    .setOrderEndTime(orderEndTime);
            if (orderSeatListMapper.getSeatRepeat(getIsRepeat) > 0) {
                throw BusinessException.of("该座位已被占用,无法续约");
            }

            // 若订单状态在使用中状态，且续约次数为0，则可以续约
            if (listState.equals(OrderConstant.listState.IN_USE) && continueCount.equals(0)) {
                updateWrapper.set(OrderSeatList::getOrderEndTime, orderEndTime)
                        .set(OrderSeatList::getContinueCount, 1)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                orderSeatListMapper.update(null, updateWrapper);
                return R.ok("续约成功");
            } else {
                throw BusinessException.of("该订单不可续约");
            }

        } finally {
            lock.unlock();
        }


    }

    /**
     * @return
     * @description: 加入拼团
     * @author: lc
     * @date: 2021/6/17 15:03
     * @params
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R joinGroup(GroupJoinReqVO groupJoinReqVO, SysUserDTO sysUserDTO) {

        // 获取传参参数
        String orderGroupId = groupJoinReqVO.getOrderGroupId();
        String tenantCode = sysUserDTO.getTenantCode();
        String username = sysUserDTO.getUsername();
        String userId = sysUserDTO.getId();
        Date nowDate = new Date();


        // 生成分布式锁key（和预约订单共用一把锁）
        String lockKey = tenantCode + ":order:join-group-key-lock:" + orderGroupId;
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        OrderGroupDetail  orderGroupDetail = new OrderGroupDetail();
        try {
            // 查询预约开始，结束时间，座位数,已拼团人数
            OrderGroupList orderGroupList = orderGroupListMapper.selectById(orderGroupId);
            Date orderStartTime = orderGroupList.getOrderStartTime();
            Date orderEndTime = orderGroupList.getOrderEndTime();
            Integer seatCount = orderGroupList.getSeatCount();
            Integer userCount = orderGroupList.getUserCount();
            Integer listState = orderGroupList.getListState();
            String buildOrgId = orderGroupList.getBuildOrgId();

            // 获取规则
            OrderRuleVO orderRule = spaceSeatService.getOrderRule(null, orderGroupList.getSeatGroupId()).getData();
            orderRule = orderSeatListService.handleOrderRule(orderRule);
            Integer signLateTime = orderRule.getSignLateTime();
            Integer signLeaveLimitTime = orderRule.getSignLeaveLimitTime();


            // 若拼团人数已达到最大值，不可预约
            if (userCount >= seatCount) {
                throw BusinessException.of("拼团人数已满，不可加入");
            }

            // 若订单状态不为待拼团,则不可拼团
            if (!listState.equals(OrderConstant.GroupListState.GROUPING)) {
                throw BusinessException.of("该订单目前不可拼团");
            }

            LambdaUpdateWrapper<OrderGroupList> groupUpdateWrapper = new LambdaUpdateWrapper<>();

            // 若人数加一小于座位数，更新主表和详情表数据
            if ((userCount + 1) < seatCount) {
                // 主表用户人数加一
                groupUpdateWrapper.set(OrderGroupList::getUserCount, userCount + 1)
                        .set(OrderGroupList::getTeamTime,nowDate)
                        .eq(OrderGroupList::getOrderGroupId, orderGroupId);
                orderGroupListMapper.update(null, groupUpdateWrapper);

                // 插入详情表数据
                orderGroupDetail.setUserId(userId)
                        .setUserName(username)
                        .setJoinTime(nowDate)
                        .setOrderGroupId(orderGroupId)
                        .setMemberType(OrderConstant.MemberType.JOIN)
                        .setOrderStartTime(orderStartTime)
                        .setOrderEndTime(orderEndTime)
                        .setUserState(OrderConstant.GroupUserState.GROUPING);
                orderGroupDetailMapper.insert(orderGroupDetail);
            }

            // 若人数加一等于座位数，更新主表和详情表状态
            if ((userCount + 1) == seatCount) {
                // 主表用户人数加一，订单状态改为使用中
                groupUpdateWrapper.set(OrderGroupList::getUserCount, userCount + 1)
                        .set(OrderGroupList::getListState, OrderConstant.GroupListState.IN_USE)
                        .eq(OrderGroupList::getOrderGroupId, orderGroupId);
                orderGroupListMapper.update(null, groupUpdateWrapper);

                // 插入详情表数据
                orderGroupDetail.setUserId(userId)
                        .setUserName(username)
                        .setOrderGroupId(orderGroupId)
                        .setJoinTime(nowDate)
                        .setMemberType(OrderConstant.MemberType.JOIN)
                        .setOrderStartTime(orderStartTime)
                        .setOrderEndTime(orderEndTime)
                        .setUserState(OrderConstant.GroupUserState.GROUPING);
                orderGroupDetailMapper.insert(orderGroupDetail);

                // 更新详情表人员状态为待签到
                LambdaUpdateWrapper<OrderGroupDetail> detailUpdateWrapper = new LambdaUpdateWrapper<>();
                detailUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_ARRIVE)
                        .eq(OrderGroupDetail::getOrderGroupId, orderGroupId);
                orderGroupDetailMapper.update(null, detailUpdateWrapper);

                // 消息队列：签到时间未签到，状态改变为未签到（全部未签到，释放资源）
                OrderMQDTO orderMQDTO = new OrderMQDTO();
                //计算延时时间(现在时间到预约打卡时间)
                DateTime arriveTime = DateUtil.offsetMinute(orderStartTime, signLateTime);
                long between = DateUtil.between(nowDate, arriveTime, DateUnit.MS);
                // 将订单id封装，传入消息队列
                HashMap<String, Object> orderMap = Maps.newHashMap();
                orderMap.put("listId", orderGroupId);
                orderMap.put("tenantCode", tenantCode);
                orderMap.put("buildOrgId", buildOrgId);
                OrderMQDTO orderLateMQDTO = orderMQDTO.setTime(Convert.toStr(between)).setRoutingKey(OrderConstant.routeKey.GROUP_ARRIVE_ROUTINGKEY).setParams(orderMap);
                rabbitMqUtils.sendOrderMsg(orderLateMQDTO);

                // 消息队列：签退时间，状态改为待签退（释放资源）
                between = DateUtil.between(nowDate, orderEndTime, DateUnit.MS);
                OrderMQDTO orderWaitLeaveMQDTO = orderMQDTO.setTime(Convert.toStr(between)).setRoutingKey(OrderConstant.routeKey.GROUP_WAIT_LEAVE_ROUTINGKEY).setParams(orderMap);
                rabbitMqUtils.sendOrderMsg(orderWaitLeaveMQDTO);

                // 消息队列：签退时间截止，状态改为未签退
                DateTime dateTime = DateUtil.offsetMinute(orderEndTime, signLeaveLimitTime);
                between = DateUtil.between(nowDate, dateTime, DateUnit.MS);
                 OrderMQDTO orderLeaveMQDTO = orderMQDTO.setTime(Convert.toStr(between)).setRoutingKey(OrderConstant.routeKey.GROUP_LEAVE_ROUTINGKEY).setParams(orderMap);
                rabbitMqUtils.sendOrderMsg(orderLeaveMQDTO);

            }

        }  finally {
            lock.unlock();
        }

        return R.ok(orderGroupDetail);
    }

    /**
     * @description: 同意待审批订单
     * @author: lc
     * @date: 2021/6/24 15:58
     * @params
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R approveAgree(String approveId) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:handle-approve-key-lock:" + approveId;
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        OrderApprove orderApprove;
        try {
            // 查看订单审批状态
            orderApprove= orderApproveMapper.selectById(approveId);
            if (!orderApprove.getApproveState().equals(OrderConstant.ApproveState.WAIT)){
                throw BusinessException.of("操作失败，该订单已审批或失效");
            }

            // 订单状态更新
            LambdaUpdateWrapper<OrderApprove> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderApprove::getApproveState,OrderConstant.ApproveState.AGREE)
                    .eq(OrderApprove::getApproveId,approveId);
            orderApproveMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }
        return R.ok(orderApprove);
    }

    /**
     * @description: 驳回审批
     * @author: lc
     * @date: 2021/6/24 17:20
     * @params
     * @return
     */
    @Override
    public R approveReject(String approveId) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:handle-approve-key-lock:" + approveId;
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        OrderApprove orderApprove;
        try {
            // 查看订单审批状态
            orderApprove= orderApproveMapper.selectById(approveId);
            if (!orderApprove.getApproveState().equals(OrderConstant.ApproveState.WAIT)){
                throw BusinessException.of("操作失败，该订单已审批或失效");
            }

            // 订单状态更新
            LambdaUpdateWrapper<OrderApprove> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderApprove::getApproveState,OrderConstant.ApproveState.REJECT)
                    .eq(OrderApprove::getApproveId,approveId);
            orderApproveMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }
        return R.ok(orderApprove);
    }


}