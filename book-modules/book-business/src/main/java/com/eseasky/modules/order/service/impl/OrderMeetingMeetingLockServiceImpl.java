package com.eseasky.modules.order.service.impl;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.CommonUtil;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.mapper.OrderMeetingInfoMapper;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.mapper.OrderMeetingUserMapper;
import com.eseasky.modules.order.service.OrderMeetingLockService;
import com.eseasky.modules.order.utils.OrderUtils;
import com.eseasky.modules.space.vo.SpaceConfVO;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class OrderMeetingMeetingLockServiceImpl implements OrderMeetingLockService {

    @Resource
    private OrderMeetingListMapper orderMeetingListMapper;

    @Resource
    private OrderMeetingInfoMapper orderMeetingInfoMapper;
    @Resource
    private OrderMeetingUserMapper orderMeetingUserMapper;

    @Autowired
    private Redisson redisson;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R lockCreatOrder(OrderMeetingList orderMeetingList, String tenantCode) {
        //对房间加锁
        String lockKey = tenantCode + ":order:creat-meeting-key-lock:" + orderMeetingList.getRoomId();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            List<Integer> stateList = Arrays.asList(OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId(), OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId());
            if (orderMeetingListMapper.getRoomRepeat(orderMeetingList.getRoomId(), orderMeetingList.getOrderStartTime(), orderMeetingList.getOrderEndTime(), stateList) > 0) {
                throw BusinessException.of(5001, "该房间已被预约，请重新选择");
            }
            //添加会议室订单
            orderMeetingListMapper.insert(orderMeetingList);
        } finally {
            lock.unlock();
        }
        return R.ok("订单生成成功");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public R<String> lockCancelOrder(String id, String tenantCode, String userId) {
        // 生成分布式锁
        String lockKey = tenantCode + ":meeting_order:back-key-lock:" + id;
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            Date nowDate = new Date();

            //获取父订单 orderMeeting
            LambdaQueryWrapper<OrderMeetingList> queryWrapper = new LambdaQueryWrapper<>();
            LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.eq(OrderMeetingList::getOrderMeetingId, id);
            OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(queryWrapper);
            CommonUtil.notNull(orderMeetingList, "订单异常");

            Integer meetingState = orderMeetingList.getState();
            SpaceConfVO conf = orderUtils.getRoomConf(orderMeetingList.getRoomId());

            Integer orderType = orderMeetingList.getOrderType();
            CommonUtil.check(Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType), "请确认订单类型是否正确");
            // 取消短租订单
            if (orderType.equals(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING)) {
                Integer signAdvanceTime = null != conf.getSignAdvanceTime() ? conf.getSignAdvanceTime() : 30;
                // 获取可打卡时间
                Date clockDate = DateUtil.offsetSecond(orderMeetingList.getOrderStartTime(), -(signAdvanceTime * 60)).toJdkDate();

                // 若在订单状态为待签到，且在可取消时间内
                CommonUtil.check(meetingState.equals(OrderConstant.MeetingStateEnum.TO_START.getAftId()) && DateUtil.compare(clockDate, nowDate) > 0, "操作失败，该订单已不可取消");
            }

            // 取消长租订单
            if (orderType.equals(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING)) {
                //待开始和进行中才可以取消订单
                CommonUtil.check(Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId()).contains(meetingState), "待开始和进行中才可以取消订单");
            }
            updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.SELF_CANCEL.getAftId())
                    .set(OrderMeetingList::getUpdateTime, nowDate)
                    .set(OrderMeetingList::getUseEndTime, nowDate)
                    .eq(OrderMeetingList::getOrderMeetingId, id);
            orderMeetingListMapper.update(null, updateWrapper);

            addCancelRecord(tenantCode, userId);
            return R.ok("订单取消成功");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 功能描述: <br>
     * 〈取消订单添加用户取消数〉
     *
     * @Param: [tenantCode, userId]
     * @Return: void
     * @Author: 王鹏滔
     * @Date: 2021/6/17 10:59
     */
    private void addCancelRecord(String tenantCode, String userId) {
        // 缓存内用户今日取消次数加一
        Long todayRestTime = OrderUtils.getTodayRestTime(new Date());
        // 查看当前用户当日取消订单次数，没有数据则创建并赋值0
        String userCanCelCountKey = tenantCode + ":order:userCancelCount:" + userId;
        Integer cancelCount = redisRepository.get(userCanCelCountKey, Integer.class);
        if (Objects.isNull(cancelCount)) {
            redisRepository.set(userCanCelCountKey, 0, todayRestTime);
        }
        redisRepository.increasing(userCanCelCountKey, 1);

        // 用户取消次数加一
        orderUtils.handleCancel(userId);
    }
}
