package com.eseasky.modules.order.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.entity.UserDetailsImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.WxUtils;
import com.eseasky.common.code.wx.MsgObject;
import com.eseasky.common.rabbitmq.dto.OrderMQDTO;
import com.eseasky.common.rabbitmq.message.RabbitMqUtils;
import com.eseasky.common.security.mobile.MobileAuthenticationToken;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.HandleBlacklistDTO;
import com.eseasky.modules.order.dto.SendNoticeDTO;
import com.eseasky.modules.order.entity.OrderBlacklist;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.entity.OrderUser;
import com.eseasky.modules.order.entity.OrderViolateDetail;
import com.eseasky.modules.order.mapper.OrderBlacklistMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.mapper.OrderUserMapper;
import com.eseasky.modules.order.mapper.OrderViolateDetailMapper;
import com.eseasky.modules.order.service.OrderMQService;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.service.OrderSeatListService;
import com.eseasky.modules.order.service.OrderViolateDetailService;
import com.eseasky.modules.order.utils.OrderNoticeUtils;
import com.eseasky.modules.order.utils.OrderUtils;
import com.eseasky.modules.order.vo.OrderSeatListVO;
import com.eseasky.modules.order.vo.request.BlacklistDetailReqVO;
import com.eseasky.modules.space.mapper.SpaceBuildMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @describe: 消息队列处理服务层
 * @title: OrderMQServiceImpl
 * @Author lc
 * @Date: 2021/4/26
 */
@Slf4j
@Service
public class OrderMQServiceImpl implements OrderMQService {

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    SpaceBuildMapper spaceBuildMapper;

    @Autowired
    OrderViolateDetailMapper orderViolateDetailMapper;

    @Autowired
    OrderViolateDetailService orderViolateDetailService;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Autowired
    WxUtils wxUtils;

    @Autowired
    OrderUtils orderUtils;

    @Autowired
    OrderBlacklistMapper orderBlacklistMapper;

    @Autowired
    OrderUserMapper orderUserMapper;

    @Autowired
    OrderSeatListService orderSeatListService;

    @Autowired
    OrderNoticeService orderNoticeService;

    /**
     * @return void
     * @description: 若该订单处于待签到状态, 将状态改为未签到
     * @author: lc
     * @date: 2021/4/27 10:59
     * @params [msg]
     */
    @Override
    public void handleLateList(Map<String, Object> msg) {

        // 获取传参
        String listId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String buildOrgId = StrUtil.toString(msg.get("buildOrgId"));
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            LambdaQueryWrapper<OrderSeatList> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(OrderSeatList::getListState, OrderSeatList::getContinueCount, OrderSeatList::getUserId).eq(OrderSeatList::getOrderSeatId, listId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(wrapper);

            if (Objects.isNull(orderSeatList)) {
                log.info("数据库未找到该订单");
                throw BusinessException.of("数据库未找到该订单");
            }

            // 初始化黑名单处理规则实体类
            HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO();
            handleBlacklistDTO.setTenantCode(tenantCode)
                    .setUserId(orderSeatList.getUserId())
                    .setViolateTime(new Date())
                    .setBuildOrgId(buildOrgId)
                    .setViolateType(OrderConstant.violateType.NO_ARRIVE);

            // 若该订单处于待签到状态,将状态改为未签到
            if (orderSeatList.getListState().equals(OrderConstant.listState.WAIT_ARRIVE)) {
                LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.NO_COME)
                        .eq(OrderSeatList::getOrderSeatId, listId);
                orderSeatListMapper.update(null, updateWrapper);

                // 用户违约次数加一
                handleViolate(orderSeatList.getUserId());

                // 处理黑名单规则
                handleBlackList(handleBlacklistDTO);

                log.info("订单状态改为未签到成功");
            }
            log.info("处理未签到订单完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }


    /**
     * @return void
     * @description: 若订单处于待签退状态将订单且续约次数没变, 改为未签退
     * @author: lc
     * @date: 2021/4/27 10:59
     * @params [msg]
     */
    @Override
    public void handleLeaveList(Map<String, Object> msg) {

        // 获取传参
        String listId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String buildOrgId = StrUtil.toString(msg.get("buildOrgId"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);

        try {
            LambdaQueryWrapper<OrderSeatList> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(OrderSeatList::getListState, OrderSeatList::getContinueCount, OrderSeatList::getUserId)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(wrapper);
            if (Objects.isNull(orderSeatList)) {
                log.info("数据库未找到该订单");
                throw BusinessException.of("数据库未找到该订单");
            }

            // 初始化黑名单处理规则实体类
            HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO();
            handleBlacklistDTO.setTenantCode(tenantCode)
                    .setUserId(orderSeatList.getUserId())
                    .setViolateTime(new Date())
                    .setBuildOrgId(buildOrgId)
                    .setViolateType(OrderConstant.violateType.NO_LEAVE);

            // 若订单处于使用待签退状态将订单,改为未签退
            if (orderSeatList.getContinueCount().equals(msg.get("continueCount")) && orderSeatList.getListState() == OrderConstant.listState.WAIT_LEAVE) {
                LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.NO_LEAVE)
                        .eq(OrderSeatList::getOrderSeatId, listId);
                orderSeatListMapper.update(null, updateWrapper);

                // 用户违约次数加一
                handleViolate(orderSeatList.getUserId());

                // 处理黑名单规则
                handleBlackList(handleBlacklistDTO);
                log.info("订单状态改为未签退成功");
            }
            log.info("处理未签退订单完成");
        } catch (BusinessException e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }


    /**
     * @return void
     * @description: 用户违约次数加一
     * @author: lc
     * @date: 2021/5/24 9:22
     * @params [userId]
     */
    public void handleViolate(String userId) {
        LambdaQueryWrapper<OrderUser> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderUser> updateWrapper = new LambdaUpdateWrapper<>();

        // 查询用户违约次数
        queryWrapper.select(OrderUser::getViolateCount)
                .eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(queryWrapper);
        Integer violateCount = orderUser.getViolateCount();

        // 违约次数加一
        if (StrUtil.isEmptyIfStr(violateCount)) {
            updateWrapper.set(OrderUser::getViolateCount, 1)
                    .eq(OrderUser::getUserId, userId);
        } else {
            updateWrapper.set(OrderUser::getViolateCount, violateCount + 1)
                    .eq(OrderUser::getUserId, userId);
        }

        orderUserMapper.update(null, updateWrapper);

    }

    /**
     * @return void
     * @description: 若订单在预约结束时间为使用中或待签到，且续约次数没变, 改为待签退
     * @author: lc
     * @date: 2021/4/27 11:36
     * @params [msg]
     */
    @Override
    public void handleWaitLeaveList(Map<String, Object> msg) {

        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        String listId = msg.get("listId").toString();
        LambdaQueryWrapper<OrderSeatList> wrapper = new LambdaQueryWrapper<>();
        try {
            wrapper.select(OrderSeatList::getListState, OrderSeatList::getContinueCount).eq(OrderSeatList::getOrderSeatId, listId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(wrapper);
            Integer listState = orderSeatList.getListState();
            if (Objects.isNull(orderSeatList)) {
                log.info("数据库未找到该订单");
                throw BusinessException.of("数据库未找到该订单");
            }
            // 若订单处于使用中状态将订单,改为待签退
            if (orderSeatList.getContinueCount().equals(msg.get("continueCount")) &&  ((listState== OrderConstant.listState.IN_USE) || (listState == OrderConstant.listState.WAIT_ARRIVE) )) {
                LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.WAIT_LEAVE)
                        .eq(OrderSeatList::getOrderSeatId, listId);
                orderSeatListMapper.update(null, updateWrapper);
                log.info("订单状态改为待签退成功");
            }
            log.info("处理使用中订单完成");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }


    /**
     * @return void
     * @description: 处理黑名单
     * @author: lc
     * @date: 2021/4/27 17:37
     * @params []
     */
    public void handleBlackList(HandleBlacklistDTO handleBlacklistDTO) {

        String tenantCode = StrUtil.toString(handleBlacklistDTO.getTenantCode());
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + handleBlacklistDTO);
            return;
        }
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        Date nowDate = new Date();

        try {
            // 获取传参信息
            String userId = handleBlacklistDTO.getUserId();
            Integer violateType = handleBlacklistDTO.getViolateType();
            Date violateTime = handleBlacklistDTO.getViolateTime();
            String buildOrgId = handleBlacklistDTO.getBuildOrgId();


            // 获取黑名单规则
            List<BlacklistDetailReqVO> orderBlacklistRules = orderBlacklistMapper.getRule(buildOrgId);
            if (orderBlacklistRules.size() == 0) {
                log.info("该建筑未配置黑名单规则");
                return;
            }

            // 获取一周前时间
            Date lastWeek = DateUtil.lastWeek().toJdkDate();

            // 若违规类型为未签到，更新违规信息(触发了两条黑名单规则，需更新两条记录)，并判断该用户是否都要进入黑名单
            if (violateType.equals(OrderConstant.violateType.NO_ARRIVE)) {
                // 插入连续未签到数据
                OrderViolateDetail orderViolateDetail = new OrderViolateDetail().setViolateTime(violateTime)
                        .setBlacklistRuleType(OrderConstant.blackRuleType.CONTINUE_NO_ARRIVE)
                        .setUserId(userId).setBuildOrgId(buildOrgId).setDelFlag("0");
                orderViolateDetailMapper.insert(orderViolateDetail);

                // 插入几天内多次签到数据
                orderViolateDetail.setViolateListId(IdUtil.simpleUUID())
                        .setBlacklistRuleType(OrderConstant.blackRuleType.ALWAYS_NO_ARRIVE);
                orderViolateDetailMapper.insert(orderViolateDetail);

                // 判断是否需要加入黑名单
                for (BlacklistDetailReqVO orderBlacklistRule : orderBlacklistRules) {
                    // 筛选连续未签到记录次数，查看是否需要进入黑名单
                    if (orderBlacklistRule.getRuleTypeId().equals(OrderConstant.blackRuleType.CONTINUE_NO_ARRIVE)) {
                        LambdaQueryWrapper<OrderViolateDetail> detailWrapper = new LambdaQueryWrapper<>();

                        detailWrapper.eq(OrderViolateDetail::getUserId, userId)
                                .eq(OrderViolateDetail::getBlacklistRuleType, OrderConstant.blackRuleType.CONTINUE_NO_ARRIVE)
                                .gt(OrderViolateDetail::getViolateTime, lastWeek);
                        Integer count = orderViolateDetailMapper.selectCount(detailWrapper);

                        // 插入黑名单生效时间
                        handleBlacklistDTO.setEffectDay(orderBlacklistRule.getRuleEffectDay());

                        // 若次数已达到限制次数，则加入黑名单，并删除以往违约记录
                        handleInBlackList(count, orderBlacklistRule.getRuleLimitCount(), handleBlacklistDTO);
                    }

                    // 筛选多次未签到记录次数，查看是否需要进入黑名单
                    if (orderBlacklistRule.getRuleTypeId().equals(OrderConstant.blackRuleType.ALWAYS_NO_ARRIVE)) {
                        LambdaQueryWrapper<OrderViolateDetail> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(OrderViolateDetail::getUserId, userId)
                                .eq(OrderViolateDetail::getBlacklistRuleType, OrderConstant.blackRuleType.ALWAYS_NO_ARRIVE)
                                .eq(OrderViolateDetail::getBuildOrgId, buildOrgId);
                        Integer count = orderViolateDetailMapper.selectCount(queryWrapper);

                        // 插入黑名单生效时间
                        handleBlacklistDTO.setEffectDay(orderBlacklistRule.getRuleEffectDay());

                        // 若次数已达到限制次数，则加入黑名单，并删除以往违约记录
                        handleInBlackList(count, orderBlacklistRule.getRuleLimitCount(), handleBlacklistDTO);
                    }
                }
            }

            // 若违规类型为未签退，更新违规信息，并判断该用户是否都要进入黑名单
            if (violateType.equals(OrderConstant.violateType.NO_BACK)) {
                //插入违约记录
                OrderViolateDetail orderViolateDetail = new OrderViolateDetail().setViolateTime(violateTime)
                        .setBlacklistRuleType(OrderConstant.blackRuleType.ALWAYS_NO_BACK)
                        .setUserId(userId).setBuildOrgId(buildOrgId).setDelFlag("0");
                orderViolateDetailMapper.insert(orderViolateDetail);

                // 判断是否需要加入黑名单
                for (BlacklistDetailReqVO orderBlacklistRule : orderBlacklistRules) {
                    // 筛选连续未签退记录次数，查看是否需要进入黑名单
                    if (orderBlacklistRule.getRuleTypeId().equals(OrderConstant.blackRuleType.ALWAYS_NO_BACK)) {
                        LambdaQueryWrapper<OrderViolateDetail> detailWrapper = new LambdaQueryWrapper<>();
                        detailWrapper.eq(OrderViolateDetail::getUserId, userId)
                                .eq(OrderViolateDetail::getBuildOrgId, buildOrgId)
                                .eq(OrderViolateDetail::getBlacklistRuleType, OrderConstant.blackRuleType.ALWAYS_NO_BACK);
                        Integer count = orderViolateDetailMapper.selectCount(detailWrapper);

                        // 插入黑名单生效时间
                        handleBlacklistDTO.setEffectDay(orderBlacklistRule.getRuleEffectDay());

                        // 若次数已达到限制次数，则加入黑名单，并删除以往违约记录
                        handleInBlackList(count, orderBlacklistRule.getRuleLimitCount(), handleBlacklistDTO);
                    }
                }
            }


            // 若违规类型为未返回，更新违规信息，并判断该用户是否都要进入黑名单
            if (violateType.equals(OrderConstant.violateType.NO_LEAVE)) {
                // 插入违约记录
                OrderViolateDetail orderViolateDetail = new OrderViolateDetail().setViolateTime(violateTime)
                        .setBlacklistRuleType(OrderConstant.blackRuleType.ALWAYS_NO_LEAVE)
                        .setUserId(userId).setBuildOrgId(buildOrgId).setDelFlag("0");
                orderViolateDetailMapper.insert(orderViolateDetail);

                // 判断是否需要加入黑名单
                for (BlacklistDetailReqVO orderBlacklistRule : orderBlacklistRules) {
                    // 筛选未返回记录次数，查看是否需要进入黑名单
                    if (orderBlacklistRule.getRuleTypeId().equals(OrderConstant.blackRuleType.ALWAYS_NO_LEAVE)) {
                        LambdaQueryWrapper<OrderViolateDetail> detailWrapper = new LambdaQueryWrapper<>();
                        detailWrapper.eq(OrderViolateDetail::getUserId, userId)
                                .eq(OrderViolateDetail::getBuildOrgId, buildOrgId)
                                .eq(OrderViolateDetail::getBlacklistRuleType, OrderConstant.blackRuleType.ALWAYS_NO_LEAVE);
                        Integer count = orderViolateDetailMapper.selectCount(detailWrapper);

                        // 插入黑名单生效时间
                        handleBlacklistDTO.setEffectDay(orderBlacklistRule.getRuleEffectDay());

                        // 若次数已达到限制次数，则加入黑名单，并删除以往违约记录
                        handleInBlackList(count, orderBlacklistRule.getRuleLimitCount(), handleBlacklistDTO);
                    }
                }
            }

            // 若违规类型为迟到，更新违规信息，并判断该用户是否都要进入黑名单
            if (violateType.equals(OrderConstant.violateType.BE_LATE)) {
                // 插入违约记录
                OrderViolateDetail orderViolateDetail = new OrderViolateDetail().setViolateTime(violateTime)
                        .setBlacklistRuleType(OrderConstant.blackRuleType.ALWAYS_BE_LATE)
                        .setUserId(userId).setBuildOrgId(buildOrgId).setDelFlag("0");
                orderViolateDetailMapper.insert(orderViolateDetail);

                // 判断是否需要加入黑名单
                for (BlacklistDetailReqVO orderBlacklistRule : orderBlacklistRules) {
                    // 筛选迟到记录次数，查看是否需要进入黑名单
                    if (orderBlacklistRule.getRuleTypeId().equals(OrderConstant.blackRuleType.ALWAYS_BE_LATE)) {
                        LambdaQueryWrapper<OrderViolateDetail> detailWrapper = new LambdaQueryWrapper<>();
                        detailWrapper.eq(OrderViolateDetail::getUserId, userId)
                                .eq(OrderViolateDetail::getBuildOrgId, buildOrgId)
                                .eq(OrderViolateDetail::getBlacklistRuleType, OrderConstant.blackRuleType.ALWAYS_BE_LATE);
                        Integer count = orderViolateDetailMapper.selectCount(detailWrapper);

                        // 插入黑名单生效时间

                        handleBlacklistDTO.setEffectDay(orderBlacklistRule.getRuleEffectDay());

                        // 若次数已达到限制次数，则加入黑名单，并删除以往违约记录
                        handleInBlackList(count, orderBlacklistRule.getRuleLimitCount(), handleBlacklistDTO);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
        log.info("黑名单处理完成");
        return;
    }


    /**
     * @return void
     * @description: 若违规次数达到限制次数，则删除过往违规记录，并加入黑名单
     * @author: lc
     * @date: 2021/4/28 17:04
     * @params [userCount, limitCount]
     */
    public void handleInBlackList(Integer userCount, Integer limitCount, HandleBlacklistDTO handleBlacklistDTO) {

        // 取出参数信息
        String userId = handleBlacklistDTO.getUserId();
        String tenantCode = handleBlacklistDTO.getTenantCode();
        Integer effectDay = handleBlacklistDTO.getEffectDay();
        Date violateTime = handleBlacklistDTO.getViolateTime();
        String buildOrgId = handleBlacklistDTO.getBuildOrgId();
        Date nowDate = new Date();

        // 若违规次数达到限制次数，删除过往违规记录
        if (userCount >= limitCount) {
            LambdaUpdateWrapper<OrderViolateDetail> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderViolateDetail::getUserId, userId);
            orderViolateDetailMapper.delete(updateWrapper);

            // 计算到期时间,将该用户预约订单(黑名单时期内)置为已取消（11）状态
            Date endDate = DateUtil.offsetDay(violateTime, effectDay).toJdkDate();
            LambdaUpdateWrapper<OrderSeatList> listWrapper = new LambdaUpdateWrapper<>();
            listWrapper.set(OrderSeatList::getListState, OrderConstant.listState.BLACK_CANCEL)
                    .eq(OrderSeatList::getUserId, userId)
                    .eq(OrderSeatList::getListState, OrderConstant.listState.WAIT_ARRIVE)
                    .ge(OrderSeatList::getOrderStartTime, violateTime)
                    .le(OrderSeatList::getOrderEndTime, endDate);
            orderSeatListMapper.update(null, listWrapper);

            // 用户黑名单次数加一
            LambdaQueryWrapper<OrderUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(OrderUser::getInBlacklistCount)
                    .eq(OrderUser::getUserId, userId);
            OrderUser orderUser = orderUserMapper.selectOne(queryWrapper);
            LambdaUpdateWrapper<OrderUser> userWrapper = new LambdaUpdateWrapper<>();
            userWrapper.set(OrderUser::getInBlacklistCount, orderUser.getInBlacklistCount() + 1)
                    .eq(OrderUser::getUserId, userId);
            orderUserMapper.update(null, userWrapper);

            // 用户加入黑名单
            OrderBlacklist orderBlacklist = new OrderBlacklist();
            orderBlacklist.setUserId(userId)
            .setBuildOrgId(buildOrgId)
            .setStartTime(nowDate)
            .setEndTime(endDate)
            .setContinueTime(effectDay * 24 * 60 * 60);
            orderBlacklistMapper.insert(orderBlacklist);

            // 通过MQ处理到期解除黑名单
            OrderMQDTO orderMQDTO = new OrderMQDTO();
            HashMap<String, Object> hashMap = Maps.newHashMap();
            hashMap.put("userId", userId);
            hashMap.put("buildOrgId", buildOrgId);
            hashMap.put("tenantCode", tenantCode);
            orderMQDTO.setTime(Convert.toStr(effectDay * 24 * 3600 * 1000) ).setParams(hashMap).setRoutingKey(OrderConstant.routeKey.OUT_BLACK_LIST_ROUTINGKEY);
            rabbitMqUtils.sendOrderMsg(orderMQDTO);
            log.info("该用户已加入黑名单");
        }
        return;
    }

    /**
     * @return void
     * @description: 处理黑名单到期解除
     * @author: lc
     * @date: 2021/4/29 9:35
     * @params [msg]
     */
    @Override
    public void handleOutBlackList(Map<String, Object> msg) {

        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            String userId = StrUtil.toString(msg.get("userId"));
            String buildOrgId = StrUtil.toString(msg.get("buildOrgId"));
            LambdaUpdateWrapper<OrderBlacklist> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(OrderBlacklist::getUserId,userId)
                    .set(OrderBlacklist::getBuildOrgId,buildOrgId);
            orderBlacklistMapper.delete(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解除黑名单异常");
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
        return;
    }

    /**
     * @return void
     * @description: 长租订单到期改为已完成，并续约
     * @author:
     * @date: 2021/5/3 15:37
     * @params [msg]
     */
    @Override
    public void handleFinishLong(Map<String, Object> msg) {

        // 获取传参
        String listId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        OrderSeatListVO orderSeatListVO = JSONObject.parseObject(JSON.toJSONString(msg.get("orderSeatListVO")), OrderSeatListVO.class);
        SysUserDTO sysUserDTO = JSONObject.parseObject(JSON.toJSONString(msg.get("sysUserDTO")), SysUserDTO.class);
        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl();
        userDetailsImpl.setSysUserDTO(sysUserDTO);
        userDetailsImpl.setTenantCode(tenantCode);
        SecurityContextHolder.getContext().setAuthentication(new MobileAuthenticationToken(userDetailsImpl, null));


        try {
            // 获取长租订单订单状态,阈值,使用时间
            LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getLongRequireTime, OrderSeatList::getLongUseTime)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);

            // 若订单状态为使用中（1,2），将订单改为已完成状态
            Integer listState = orderSeatList.getListState();
            if (listState.equals(1) || listState.equals(2)) {
                LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.FINISH)
                        .eq(OrderSeatList::getOrderSeatId, listId);
                orderSeatListMapper.update(null, updateWrapper);
                log.info("长租订单已完成");
            }

            // 若该订单使用时长已达到阈值，自动续约该座位
            Integer longUseTime = orderSeatList.getLongUseTime();
            Integer longRequireTime = orderSeatList.getLongRequireTime();
            if (longRequireTime.equals(-1) || Objects.isNull(longUseTime)) {
                log.info("长租续约失败");
                return;
            }

            // 更新长租订单开始时间,结束时间
            Date orderStartTime = orderSeatListVO.getOrderStartTime();
            Date orderEndTime = orderSeatListVO.getOrderEndTime();
            Integer rangeTime =Convert.toInt( DateUtil.betweenDay(orderStartTime, orderEndTime, false));
            orderSeatListVO.setOrderStartTime(orderEndTime)
            .setOrderEndTime(DateUtil.offsetDay(orderEndTime, rangeTime));


            if (longUseTime / rangeTime > longRequireTime) {
                orderSeatListService.creatOrderList(orderSeatListVO, sysUserDTO);
                log.info("长租续约成功");
            } else {
                log.info("学习时间达不到阈值，续约失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

    /**
     * @return void
     * @description: 处理暂离未返回的订单
     * @author: lc
     * @date: 2021/5/8 10:39
     * @params [msg]
     */
    @Override
    public void handleAwayBack(Map<String, Object> msg) {

        // 获取传参
        String listId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));
        String userId = StrUtil.toString(msg.get("userId"));
        String orgId = StrUtil.toString(msg.get("buildOrgId"));
        String orgId2 = StrUtil.toString(msg.get("buildOrgId"));


        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);

        try {
            // 判断订单状态是否为暂离，若为暂离则将订单改为违规状态
            LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderSeatId, OrderSeatList::getUserId)
                    .eq(OrderSeatList::getOrderSeatId, listId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);

            // 初始化黑名单处理规则实体类
            HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO();
            handleBlacklistDTO.setTenantCode(tenantCode)
                    .setUserId(userId)
                    .setViolateTime(new Date())
                    .setBuildOrgId(orgId)
                    .setViolateType(OrderConstant.violateType.NO_BACK);

            if (orderSeatList.getListState().equals(OrderConstant.listState.AWAY)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.NO_BACK)
                        .eq(OrderSeatList::getOrderSeatId, listId);
                orderSeatListMapper.update(null, updateWrapper);
            }

            // 用户违约次数加一
            handleViolate(orderSeatList.getUserId());

            // 处理黑名单规则
            handleBlackList(handleBlacklistDTO);

            log.info("订单状态修改为违规（暂离未返回）");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }

    @Override
    public void clockReminderMessage(Map<String, Object> msg) {

        // 获取传参
        String listId = StrUtil.toString(msg.get("listId"));
        String tenantCode = StrUtil.toString(msg.get("tenantCode"));

        if (StrUtil.isEmpty(tenantCode)) {
            log.info("传参信息有误：" + msg);
            return;
        }

        DynamicDataSourceContextHolder.setDataSourceKey(tenantCode);
        try {
            //获取订单
            LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderSeatList::getOrderSeatId, listId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNull(orderSeatList)) {
                log.error("订单不存在,orderListId：" + listId);
                return;
            }
            //获取用户信息
            SysUserDTO sysUserDTO = JSONObject.parseObject(JSON.toJSONString(msg.get("sysUserDTO")), SysUserDTO.class);
            //待签到，使用中给他提醒
            if (Arrays.asList(OrderConstant.listState.WAIT_ARRIVE, OrderConstant.listState.IN_USE).contains(orderSeatList.getListState())) {
                JSONObject meetingClockWxMsg = orderUtils.getClockWxMsg(sysUserDTO.getUsername(), sysUserDTO.getOpenid(), orderSeatList.getOrderStartTime(),"座位打卡");

                //发送微信通知消息
                if (StringUtils.isNotBlank(sysUserDTO.getOpenid())) {
                    MsgObject result = wxUtils.sendMsg(meetingClockWxMsg);
                    if (0 != result.getErrcode() && !"ok".equalsIgnoreCase(result.getErrmsg())) {
                        log.error("OrderMQServiceImpl.clockReminderMessage 发送微信通知错误  msg：" + result.getErrmsg());
                    } else {
                        //微信通知
                        SendNoticeDTO noticeWechat = OrderNoticeUtils.getSendNoticeDTO(sysUserDTO.getId(), OrderConstant.NoticeType.WeChat, OrderConstant.NoticeTitleType.SEAT, null, "座位打卡", JSONObject.toJSONString(meetingClockWxMsg));
                        orderNoticeService.sendNotice(noticeWechat);
                    }
                }
            } else {
                log.info("非待签到和使用中状态，state：" + orderSeatList.getListState());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
    }


}