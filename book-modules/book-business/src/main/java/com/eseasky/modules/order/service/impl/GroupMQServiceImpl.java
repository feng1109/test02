package com.eseasky.modules.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.HandleBlacklistDTO;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.eseasky.modules.order.mapper.OrderGroupDetailMapper;
import com.eseasky.modules.order.mapper.OrderGroupListMapper;
import com.eseasky.modules.order.service.GroupMQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @describe:
 * @title: GroupMQServiceImpl
 * @Author lc
 * @Date: 2021/6/16
 */
@Slf4j
@Service
public class GroupMQServiceImpl implements GroupMQService {


    @Autowired
    OrderGroupListMapper orderGroupListMapper;


    @Autowired
    OrderGroupDetailMapper orderGroupDetailMapper;

    @Autowired
    OrderMQServiceImpl orderMQService;

    /**
     * @return
     * @description: 处理拼团截止
     * @author: lc
     * @date: 2021/6/16 10:14
     * @params
     */
    @Override
    public void handleGroupOff(Map<String, Object> msg) {

        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String listId = StrUtil.toString(msg.get("listId"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);

        try {
            // 查询订单状态
            LambdaQueryWrapper<OrderGroupList> groupQueryWrapper = new LambdaQueryWrapper<>();
            groupQueryWrapper.select(OrderGroupList::getListState)
                    .eq(OrderGroupList::getOrderGroupId, listId);
            OrderGroupList orderGroupList = orderGroupListMapper.selectOne(groupQueryWrapper);

            // 若为拼团中，订单改为拼团失败
            if (orderGroupList.getListState().equals(OrderConstant.GroupListState.GROUPING)) {
                LambdaUpdateWrapper<OrderGroupList> groupUpdateWrapper = new LambdaUpdateWrapper<>();
                groupUpdateWrapper.set(OrderGroupList::getListState, OrderConstant.GroupListState.GROUP_FAIL)
                        .eq(OrderGroupList::getOrderGroupId, listId);
                orderGroupListMapper.update(null, groupUpdateWrapper);

                // 人员订单状态更新为拼团失败
                LambdaUpdateWrapper<OrderGroupDetail> detailUpdateWrapper = new LambdaUpdateWrapper<>();
                detailUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.GROUP_FAIL)
                        .eq(OrderGroupDetail::getOrderGroupId, listId);
                orderGroupDetailMapper.update(null, detailUpdateWrapper);
                log.info("消息队列：拼团状态已改为拼团失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

    /**
     * @return
     * @description: 拼团订单签到
     * @author: lc
     * @date: 2021/6/28 14:21
     * @params
     */
    @Override
    public void handleGroupArrive(Map<String, Object> msg) {
        // 获取传参
        String orderGroupId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String buildOrgId = StrUtil.toString(msg.get("buildOrgId"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            // 查看待签到（未打卡）人数
            LambdaQueryWrapper<OrderGroupDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_ARRIVE);
            Integer count = orderGroupDetailMapper.selectCount(detailQueryWrapper);

            // 未签到人数等于座位人数，订单状态置为已完成释放资源
            OrderGroupList orderGroupList = orderGroupListMapper.selectById(orderGroupId);
            Integer seatCount = orderGroupList.getSeatCount();
            if (seatCount.equals(count)) {
                LambdaUpdateWrapper<OrderGroupList> listUpdateWrapper = new LambdaUpdateWrapper<>();
                listUpdateWrapper.set(OrderGroupList::getListState, OrderConstant.GroupListState.FINISH)
                        .eq(OrderGroupList::getListState,OrderConstant.GroupListState.IN_USE)
                        .eq(OrderGroupList::getOrderGroupId, orderGroupId);
                orderGroupListMapper.update(null, listUpdateWrapper);
            }

            // 查看待签到人员名单
            LambdaQueryWrapper<OrderGroupDetail> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper.select(OrderGroupDetail::getUserId)
                    .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_ARRIVE);
            List<OrderGroupDetail> orderGroupDetails = orderGroupDetailMapper.selectList(detailQueryWrapper);

            // 将待签到人员状态置为未签到
            LambdaUpdateWrapper<OrderGroupDetail> detailUpdateWrapper = new LambdaUpdateWrapper<>();
            detailUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.NO_ARRIVE)
                    .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_ARRIVE);
            orderGroupDetailMapper.update(null, detailUpdateWrapper);

            // 未签到人员处理黑名单规则
            for (OrderGroupDetail orderGroupDetail : orderGroupDetails) {
                // 初始化黑名单处理规则实体类
                HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO();
                handleBlacklistDTO.setTenantCode(tenantCode)
                        .setUserId(orderGroupDetail.getUserId())
                        .setViolateTime(new Date())
                        .setBuildOrgId(buildOrgId)
                        .setViolateType(OrderConstant.violateType.NO_ARRIVE);

                // 处理黑名单规则
                orderMQService.handleBlackList(handleBlacklistDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }


    }

    /**
     * @return
     * @description: 拼团订单结束时间代为待签到（释放资源）
     * @author: lc
     * @date: 2021/6/28 14:21
     * @params
     */
    @Override
    public void  handleGroupWaitLeave(Map<String, Object> msg) {
        // 获取传参
        String orderGroupId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String buildOrgId = StrUtil.toString(msg.get("buildOrgId"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            // 将总订单置为已完成状态
            LambdaUpdateWrapper<OrderGroupList> listUpdateWrapper = new LambdaUpdateWrapper<>();
            listUpdateWrapper.set(OrderGroupList::getListState, OrderConstant.GroupListState.FINISH)
                    .eq(OrderGroupList::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupList::getListState, OrderConstant.GroupListState.IN_USE);
            orderGroupListMapper.update(null, listUpdateWrapper);

            // 将处于使用中的人员状态置为待签退
            LambdaUpdateWrapper<OrderGroupDetail> detailLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            detailLambdaUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_LEAVE)
                    .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.IN_USE);
            orderGroupDetailMapper.update(null, detailLambdaUpdateWrapper);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

    /**
     * @return
     * @description: 拼团签退截止时间
     * @author: lc
     * @date: 2021/6/28 16:33
     * @params
     */
    @Override
    public void handleGroupLeave(Map<String, Object> msg) {

        // 获取传参
        String orderGroupId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String buildOrgId = StrUtil.toString(msg.get("buildOrgId"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            // 查询所有待签人员id
            LambdaQueryWrapper<OrderGroupDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(OrderGroupDetail::getUserId)
                    .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_LEAVE);
            List<OrderGroupDetail> orderGroupDetails = orderGroupDetailMapper.selectList(queryWrapper);


            // 将待签退人员状态改为未签退
            LambdaUpdateWrapper<OrderGroupDetail> listUpdateWrapper = new LambdaUpdateWrapper<>();
            listUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.NO_LEAVE)
                    .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                    .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.WAIT_LEAVE);
            orderGroupDetailMapper.update(null, listUpdateWrapper);

            // 未签退人员黑名单规则处理
            for (OrderGroupDetail orderGroupDetail : orderGroupDetails) {
                // 初始化黑名单处理规则实体类
                HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO();
                handleBlacklistDTO.setTenantCode(tenantCode)
                        .setUserId(orderGroupDetail.getUserId())
                        .setViolateTime(new Date())
                        .setBuildOrgId(buildOrgId)
                        .setViolateType(OrderConstant.violateType.NO_LEAVE);

                // 处理黑名单规则
                orderMQService.handleBlackList(handleBlacklistDTO);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }

    }


}