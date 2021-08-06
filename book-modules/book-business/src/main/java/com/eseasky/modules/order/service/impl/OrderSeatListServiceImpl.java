package com.eseasky.modules.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.dto.OrgDTO;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.common.rabbitmq.dto.OrderMQDTO;
import com.eseasky.common.rabbitmq.message.RabbitMqUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.*;
import com.eseasky.modules.order.entity.*;
import com.eseasky.modules.order.mapper.*;
import com.eseasky.modules.order.service.OrderBlacklistService;
import com.eseasky.modules.order.service.OrderLockService;
import com.eseasky.modules.order.service.OrderSeatListService;
import com.eseasky.modules.order.service.OrderUserService;
import com.eseasky.modules.order.vo.OrderRuleVO;
import com.eseasky.modules.order.vo.OrderSeatListVO;
import com.eseasky.modules.order.vo.request.*;
import com.eseasky.modules.order.vo.response.*;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceBuildMapper;
import com.eseasky.modules.space.mapper.SpaceFloorMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.mapper.SpaceSeatMapper;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.request.QueryOrderSeatParam;
import com.eseasky.modules.space.vo.response.SeatAndGroupForOrder;
import com.eseasky.modules.space.vo.response.SeatInfoToOrder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2021-04-12
 */
@Slf4j
@Service
@Transactional
public class OrderSeatListServiceImpl extends ServiceImpl<OrderSeatListMapper, OrderSeatList> implements OrderSeatListService {

    @Autowired
    OrderUserService orderUserService;

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    OrderSeatListService orderSeatListService;

    @Autowired
    SpaceBuildMapper spaceBuildMapper;

    @Autowired
    SpaceFloorMapper spaceFloorMapper;

    @Autowired
    SpaceRoomMapper spaceRoomMapper;

    @Autowired
    SpaceSeatMapper spaceSeatMapper;

    @Autowired
    SpaceSeatService spaceSeatService;

    @Autowired
    OrderUserMapper orderUserMapper;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    OrderBlacklistService orderBlacklistService;

    @Autowired
    OrderViolateDetailMapper orderViolateDetailMapper;

    @Autowired
    Redisson redisson;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Autowired
    OrderLockService orderLockService;

    @Autowired
    OrderBlacklistMapper orderBlacklistMapper;

    @Autowired
    OrderLongRentDetailMapper orderLongRentDetailMapper;

    @Autowired
    OrderApproveServiceImpl orderApproveService;

    @Autowired
    OrderGroupListMapper orderGroupListMapper;

    @Autowired
    OrderGroupDetailMapper orderGroupDetailMapper;

    @Autowired
    SpaceUtil spaceUtil;

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 测试
     * @author: lc
     * @date: 2021/4/16 13:32
     * @params []
     */
    @Override
    public R test() {
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        return null;
    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: pc端查看统计
     * @author: lc
     * @date: 2021/5/8 14:10
     * @params [statisticsDataReqVO]
     */
    @Override
    public R showPCStatisticsData(StatisticsDataReqVO statisticsDataReqVO) {

        // 获取传参数据
        String month = statisticsDataReqVO.getMonth();
        Integer statisticsType = statisticsDataReqVO.getStatisticsType();
        Map<String, Integer> collect = Maps.newHashMap();

        // 查看统计数据
        if (statisticsType < OrderConstant.statisticsType.SPACE) {
            List<StatisticsLearnTimeDTO> statisticsDate = orderSeatListMapper.getStatisticsDate(month, statisticsType);
            collect = statisticsDate.stream().collect(Collectors.toMap(el -> StrUtil.toString(el.getDay()), el -> el.getCount()));
        }

        // 查看空间排行
        if (statisticsType == OrderConstant.statisticsType.SPACE) {
            List<StatisticsLearnTimeDTO> spaceTop = orderSeatListMapper.getSpaceTop(month);
            collect = spaceTop.stream().collect(Collectors.toMap(el -> el.getRoomName(), el -> el.getCount()));
        }

        // 查看学生排行
        if (statisticsType == OrderConstant.statisticsType.STUDENT) {
            List<StatisticsLearnTimeDTO> studentTop = orderSeatListMapper.getStudentTop(month);
            collect = studentTop.stream().collect(Collectors.toMap(el -> el.getUserName(), el -> el.getCount()));
        }

        return R.ok(collect);
    }


    /**
     * @return java.util.HashMap
     * @description: 查看房间非空闲座位状态
     * @author: lc
     * @date: 2021/4/15 15:38
     * @params [orderListInfoDTO]
     */
    @Override
    public List<Map<String, Object>> getUsedSeat(OrderListInfoDTO orderListInfoDTO) {

        // 若endTime为空，返回座位状态
        if (Objects.isNull(orderListInfoDTO.getEndTime())) {
            LambdaQueryWrapper<OrderSeatList> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(OrderSeatList::getSeatId, OrderSeatList::getListState).eq(OrderSeatList::getRoomId, orderListInfoDTO.getRoomId())
                    .ge(OrderSeatList::getOrderEndTime, orderListInfoDTO.getStartTime()).le(OrderSeatList::getOrderStartTime, orderListInfoDTO.getStartTime());
            return orderSeatListMapper.selectMaps(wrapper);
        }

        // 若endTime不为空，返回座位状态
        return orderSeatListMapper.getUsedSeat(orderListInfoDTO);
    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端：首页（最近一次预约，及登陆后展示）
     * @author: lc
     * @date: 2021/4/20 10:41
     * @params [userId]
     */
    @Override
    public R showLastTimeList(String userId) {


        // 查看最近一次短租订单,算出预约时长放入集合
        FirstPageRePVO shortLastList = orderSeatListMapper.getShortLastList(userId);

        // 处理单人短租数据
        if (Objects.nonNull(shortLastList)) {
            Integer orderType = shortLastList.getOrderType();
            String seatId = shortLastList.getSeatId();
            if (orderType.equals(OrderConstant.OrderType.SHORT_RENT)) {
                shortLastList = handleShortLastList(shortLastList);

                // 获取打卡方式和座位名称
                R<SeatAndGroupForOrder> seatInfoForOrder2 = spaceSeatService.getSeatAndGroupForOrder(seatId, null);
                if (seatInfoForOrder2.getCode() == 1) {
                    throw BusinessException.of(seatInfoForOrder2.getMsg());
                }
                SeatAndGroupForOrder data = seatInfoForOrder2.getData();
                shortLastList.setSeatNum(data.getSeatNum())
                        .setClockType(Convert.toInt(data.getSignId()));
            }
        }

        // 处理拼团订单
        if (Objects.nonNull(shortLastList)) {
            Integer orderType = shortLastList.getOrderType();
            String seatId = shortLastList.getSeatId();
            if (orderType.equals(OrderConstant.OrderType.GROUP_RENT)) {
                shortLastList = handleGroupLastList(shortLastList);

                // 获取打卡方式和座位名称
                R<SeatAndGroupForOrder> seatInfoForOrder2 = spaceSeatService.getSeatAndGroupForOrder(null, seatId);
                SeatAndGroupForOrder seatInfoForOrder = seatInfoForOrder2.getData();
                String msg = seatInfoForOrder2.getMsg();
                if (Objects.isNull(seatInfoForOrder)) {
                    throw BusinessException.of(msg);
                } else {
                    shortLastList.setSeatNum(seatInfoForOrder.getGroupName())
                            .setClockType(Convert.toInt(seatInfoForOrder.getSignId()));
                }
            }
        }

        // 处理最近一次长租订单
        FirstPageRePVO longLastList = orderSeatListMapper.getLongLastList(userId);

        if (Objects.nonNull(longLastList)) {
            String seatId = longLastList.getSeatId();
            Date longEndTime = longLastList.getOrderEndTime();
            Date longStartTime = longLastList.getOrderStartTime();
            longLastList.setStartTime(DateUtil.formatTime(longStartTime)).
                    setEndTime(DateUtil.formatTime(longEndTime))
                    .setStartDay(DatePattern.CHINESE_DATE_FORMAT.format(longStartTime))
                    .setEndDay(DatePattern.CHINESE_DATE_FORMAT.format(longEndTime));

            // 获取打卡方式和座位名称
            R<SeatAndGroupForOrder> seatInfoForOrder2 = spaceSeatService.getSeatAndGroupForOrder(seatId, null);
            if (seatInfoForOrder2.getCode() == 1) {
                throw BusinessException.of(seatInfoForOrder2.getMsg());
            }
            SeatAndGroupForOrder data = seatInfoForOrder2.getData();
            longLastList.setSeatNum(data.getSeatNum())
                    .setClockType(Convert.toInt(data.getSignId()));

            // 将使用时长转为单位h
            Integer useTime = new Integer(longLastList.getLongUseTime());
            double d = useTime / (3600.0);
            longLastList.setLongUseTime(new DecimalFormat("#.#").format(d) + "小时");
        }
        ArrayList<Object> orderMaps = Lists.newArrayList(shortLastList, longLastList);
        return R.ok(orderMaps);

    }

    /**
     * @return
     * @description: 首页：处理单人短租订单
     * @author: lc
     * @date: 2021/6/21 15:59
     * @params
     */
    public FirstPageRePVO handleShortLastList(FirstPageRePVO shortLastList) {

        // 根据座位id获取规则
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(shortLastList.getSeatId(), null).getData();
        orderRule = handleOrderRule(orderRule);
        Integer signAdvanceTime = orderRule.getSignAdvanceTime();
        Integer signAwayLimitTime = orderRule.getSignAwayLimitTime();
        Integer signLeaveLimitTime = orderRule.getSignLeaveLimitTime();
        Integer signLateTime = orderRule.getSignLateTime();

        // 插入订单开始结束时间，及日期
        Date orderEndTime = shortLastList.getOrderEndTime();
        Date orderStartTime = shortLastList.getOrderStartTime();
        shortLastList.setStartTime(DateUtil.formatTime(orderStartTime))
                .setEndTime(DateUtil.formatTime(orderEndTime))
                .setStartDay(DatePattern.CHINESE_DATE_FORMAT.format(orderStartTime))
                .setEndDay(DatePattern.CHINESE_DATE_FORMAT.format(orderEndTime));

        // 若订单状态为待使用，返回可签到时间
        if (shortLastList.getListState().equals(OrderConstant.listState.WAIT_ARRIVE)) {
            // 返回可签到时间
            Date ableArriveTime = DateUtil.offsetMinute(orderStartTime, -(signAdvanceTime)).toJdkDate();
            shortLastList.setAbleArriveTime(ableArriveTime);

            // 返回允许迟到时间
            DateTime allowLateTime = DateUtil.offsetMinute(orderStartTime, signLateTime);
            shortLastList.setAllowLateTime(allowLateTime);

        }

        // 若订单状态为使用中，返回可暂离时间
        if (shortLastList.getListState().equals(OrderConstant.listState.IN_USE)) {
            Date ableAwayTime = DateUtil.offsetMinute(orderEndTime, -signAwayLimitTime).toJdkDate();
            shortLastList.setAbleAwayTime(ableAwayTime);
        }

        // 若状态为暂离，返回暂离可返回时间
        if (shortLastList.getListState().equals(OrderConstant.listState.AWAY)) {
            Date ableBackTime = DateUtil.offsetMinute(shortLastList.getAwayTime(), signAwayLimitTime).toJdkDate();
            shortLastList.setAbleBackTime(ableBackTime);
        }

        // 若状态为待签退，返回课前可签退时间
        if (shortLastList.getListState().equals(OrderConstant.listState.WAIT_LEAVE)) {
            Date ableLeaveTime = DateUtil.offsetMinute(orderEndTime, signLeaveLimitTime).toJdkDate();
            shortLastList.setAbleLeaveTime(ableLeaveTime);
        }

        try {
            // 计算并插入预约时长
            String orderStartStr = DatePattern.NORM_DATETIME_FORMAT.format(orderStartTime);
            String orderEndStr = DatePattern.NORM_DATETIME_FORMAT.format(orderEndTime);
            String orderTime = twoTimeDiffer(orderStartStr, orderEndStr);
            shortLastList.setOrderTimeRange(orderTime + "小时");
        } catch (ParseException e) {
            throw BusinessException.of("时间类型转换错误");
        }

        return shortLastList;
    }

    /**
     * @return
     * @description: 首页：处理拼团订单
     * @author: lc
     * @date: 2021/6/21 15:59
     * @params
     */
    public FirstPageRePVO handleGroupLastList(FirstPageRePVO shortLastList) {
        // 根据座位id获取规则
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(null, shortLastList.getSeatId()).getData();
        orderRule = handleOrderRule(orderRule);
        Integer signAdvanceTime = orderRule.getSignAdvanceTime();
        Integer signLeaveLimitTime = orderRule.getSignLeaveLimitTime();
        Integer signLateTime = orderRule.getSignLateTime();

        // 插入订单开始结束时间，及日期
        Date orderEndTime = shortLastList.getOrderEndTime();
        Date orderStartTime = shortLastList.getOrderStartTime();
        shortLastList.setStartTime(DateUtil.formatTime(orderStartTime))
                .setEndTime(DateUtil.formatTime(orderEndTime))
                .setStartDay(DatePattern.CHINESE_DATE_FORMAT.format(orderStartTime))
                .setEndDay(DatePattern.CHINESE_DATE_FORMAT.format(orderEndTime));

        // 若订单状态为待使用，返回可签到时间
        if (shortLastList.getListState().equals(OrderConstant.GroupUserState.WAIT_ARRIVE)) {
            // 返回可签到时间
            Date ableArriveTime = DateUtil.offsetMinute(orderStartTime, -(signAdvanceTime)).toJdkDate();
            shortLastList.setAbleArriveTime(ableArriveTime);

            // 返回允许迟到时间
            DateTime allowLateTime = DateUtil.offsetMinute(orderStartTime, signLateTime);
            shortLastList.setAllowLateTime(allowLateTime);
        }

        // 若状态为待签退，返回课前可签退时间
        if (shortLastList.getListState().equals(OrderConstant.GroupUserState.WAIT_LEAVE)) {
            Date ableLeaveTime = DateUtil.offsetMinute(orderEndTime, signLeaveLimitTime).toJdkDate();
            shortLastList.setAbleLeaveTime(ableLeaveTime);
        }

        try {
            // 计算并插入预约时长
            String orderStartStr = DatePattern.NORM_DATETIME_FORMAT.format(orderStartTime);
            String orderEndStr = DatePattern.NORM_DATETIME_FORMAT.format(orderEndTime);
            String orderTime = twoTimeDiffer(orderStartStr, orderEndStr);
            shortLastList.setOrderTimeRange(orderTime + "小时");
        } catch (ParseException e) {
            throw BusinessException.of("时间类型转换错误");
        }

        return shortLastList;
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端：查看订单详情
     * @author: lc
     * @date: 2021/4/20 18:23
     * @params [orderSeatId]
     */
    @Override
    public R showListDetail(String orderSeatId) {

        //获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String username = sysUserDTO.getUsername();


        OrderListDetailVO orderListDetailVO = orderSeatListMapper.showListDetail(orderSeatId);
        orderListDetailVO.setUserName(username);
        Integer orderType = orderListDetailVO.getOrderType();

        Date orderStartTime = orderListDetailVO.getOrderStartTime();
        Date orderEndTime = orderListDetailVO.getOrderEndTime();
        // 短租拼接返回时间
        if (OrderConstant.OrderType.SHORT_RENT.equals(orderType)) {
            String time = DateUtil.formatDate(orderStartTime) + " " + DateUtil.formatTime(orderStartTime) + " ~ " + DateUtil.formatTime(orderEndTime);
            orderListDetailVO.setTime(time);
        }

        // 长租拼接返回时间
        if (OrderConstant.OrderType.LONG_RENT.equals(orderType)) {
            String time = DateUtil.formatDate(orderStartTime) + " ~ " + DateUtil.formatDate(orderEndTime);
            orderListDetailVO.setTime(time);
        }

        return R.ok(orderListDetailVO);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端：查看用户短租订单
     * @author: lc
     * @date: 2021/4/21 9:37
     * @params [orderListReqVO]
     */
    @Override
    public R userShortRentList(UserListReqVO userListReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        userListReqVO.setUserId(sysUserDTO.getId());

        // 计算查看订单截止日期
        Integer timeRange = userListReqVO.getTimeRange();
        if (!timeRange.equals(0)) {
            Date date = DateUtil.offsetMonth(new Date(), -timeRange).toJdkDate();
            userListReqVO.setEndTime(date);
        }

        // 查看短租订单,计算预约时长（单位：h）
        Page<Object> page = new Page<>(userListReqVO.getPageNum(), userListReqVO.getPageSize());
        List<UserRentListVO> userShortRent = orderSeatListMapper.getUserShortRent(page, userListReqVO);

        // 计算时间
        for (UserRentListVO userRentListVO : userShortRent) {
            try {
                String orderTime = twoTimeDiffer(DateUtil.formatDateTime(userRentListVO.getOrderStartTime()), DateUtil.formatDateTime(userRentListVO.getOrderEndTime()));
                userRentListVO.setShortOrderTime(orderTime + "小时");

                // 拼接前端展示格式
                Date orderStartTime = userRentListVO.getOrderStartTime();
                Date orderEndTime = userRentListVO.getOrderEndTime();
                String startTime = "";
                String endTime = "";
                if (Objects.nonNull(orderStartTime)) {
                    startTime = DatePattern.CHINESE_DATE_FORMAT.format(orderStartTime) + StrUtil.subWithLength(DatePattern.NORM_TIME_FORMAT.format(orderStartTime), 0, 5);
                }
                if (Objects.nonNull(orderEndTime)) {
                    endTime = DatePattern.CHINESE_DATE_FORMAT.format(orderEndTime) + StrUtil.subWithLength(DatePattern.NORM_TIME_FORMAT.format(orderEndTime), 0, 5);
                }
                userRentListVO.setStartTime(startTime).setEndTime(endTime);

            } catch (ParseException e) {
                throw BusinessException.of("时间类型转换错误");
            }
        }

        // 初始化返回对象
        ListPageRePVO listPageRePVO = new ListPageRePVO();
        listPageRePVO.setPages(Convert.toInt(page.getPages()))
                .setTotal(Convert.toInt(page.getTotal()))
                .setListVOS(userShortRent);

        return R.ok(listPageRePVO);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端：查看用户长租订单
     * @author: lc
     * @date: 2021/4/21 11:23
     * @params [orderListReqVO]
     */
    @Override
    public R userLongRentList(UserListReqVO userListReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        userListReqVO.setUserId(sysUserDTO.getId());

        // 计算查看订单截止日期
        Integer timeRange = userListReqVO.getTimeRange();
        if (!timeRange.equals(0)) {
            Date date = DateUtil.offsetMonth(new Date(), -timeRange).toJdkDate();
            userListReqVO.setEndTime(date);
        }

        // 查看短租订单,计算预约时长（单位：h）
        Page<Object> page = new Page<>(userListReqVO.getPageNum(), userListReqVO.getPageSize());
        List<UserRentListVO> userLongRent = orderSeatListMapper.getUserLongRent(page, userListReqVO);


        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        for (UserRentListVO userRentListVO : userLongRent) {
            Double aDouble = new Double(userRentListVO.getLongUseTime());
            String useTime = decimalFormat.format(aDouble / (3600)) + "小时";
            userRentListVO.setLongUseTime(useTime);

            // 拼接前端展示格式
            Date orderStartTime = userRentListVO.getOrderStartTime();
            Date orderEndTime = userRentListVO.getOrderEndTime();
            String startTime = "";
            String endTime = "";
            if (Objects.nonNull(orderStartTime)) {
                startTime = DatePattern.CHINESE_DATE_FORMAT.format(orderStartTime);
            }
            if (Objects.nonNull(orderEndTime)) {
                endTime = DatePattern.CHINESE_DATE_FORMAT.format(orderEndTime);
            }
            userRentListVO.setStartTime(startTime).setEndTime(endTime);
        }

        // 初始化返回对象
        ListPageRePVO listPageRePVO = new ListPageRePVO();
        listPageRePVO.setPages(Convert.toInt(page.getPages()))
                .setTotal(Convert.toInt(page.getTotal()))
                .setListVOS(userLongRent);

        return R.ok(listPageRePVO);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端：长租签到记录
     * @author: lc
     * @date: 2021/4/21 16:17
     * @params [orderSeatId]
     */
    @Override
    public R showLongRentDetail(LongDetailReqVO longDetailReqVO) {

        // 查看长租签到记录
        LambdaQueryWrapper<OrderLongRentDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OrderLongRentDetail::getUseStartTime, OrderLongRentDetail::getUseEndTime, OrderLongRentDetail::getIsLeave)
                .eq(OrderLongRentDetail::getOrderSeatId, longDetailReqVO.getOrderSeatId())
                .orderByDesc(OrderLongRentDetail::getUseStartTime);
        Page<OrderLongRentDetail> page = new Page<>(longDetailReqVO.getPageNum(), longDetailReqVO.getPageSize());
        Page<OrderLongRentDetail> orderLongRentDetailPage = orderLongRentDetailMapper.selectPage(page, wrapper);
        return R.ok(orderLongRentDetailPage);
    }

    /**
     * @return
     * @description: 显示常用地点
     * @author: lc
     * @date: 2021/6/22 16:09
     * @params
     */
    @Override
    public R showOftenUseArea(ShowOftenUseAreaReqVO showOftenUseAreaReqVO) {
        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        // 获取传参信息
        String coordx = showOftenUseAreaReqVO.getCoordx();
        String coordy = showOftenUseAreaReqVO.getCoordy();

        // 获取常用建筑，和使用次数
        List<OftenUserAreaRepVO> oftenUseArea = orderSeatListMapper.getOftenUseArea(userId);

        // 若没有常用数据，获取最近的楼层信息
        if (CollectionUtil.isEmpty(oftenUseArea)) {
            // 获取最近的楼层信息
            List<GetShortestBuildDTO> shortestBuilds = spaceBuildMapper.getShortestBuild(coordx, coordy);
            // 计算距离
            for (GetShortestBuildDTO shortestBuild : shortestBuilds) {
                GlobalCoordinates from = new GlobalCoordinates(Convert.toDouble(coordy),Convert.toDouble(coordx));
                GlobalCoordinates to = new GlobalCoordinates(Convert.toDouble(shortestBuild.getCoordy()),Convert.toDouble(shortestBuild.getCoordx()));
                Integer distanceMeter =Convert.toInt(spaceUtil.getDistanceMeter(from, to, Ellipsoid.WGS84));

                OftenUserAreaRepVO oftenUserAreaRepVO = JSONObject.parseObject(JSON.toJSONString(shortestBuild), OftenUserAreaRepVO.class);
                oftenUserAreaRepVO.setType(2)
                        .setDistance(distanceMeter);
                oftenUseArea.add(oftenUserAreaRepVO);
            }
        }
        return R.ok(oftenUseArea);
    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端：查看用户信息
     * @author: lc
     * @date: 2021/4/21 20:49
     * @params [userId]
     */
    @Override
    public R showUserInfo() {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String username = sysUserDTO.getUsername();
        String phone = sysUserDTO.getPhone();
        String userNo = sysUserDTO.getUserNo();
        List<OrgDTO> sysOrgs = sysUserDTO.getSysOrgs();

        // 获取用户所属院系
        String[] orgs = sysOrgs.stream().map(OrgDTO::getOrgName).toArray(String[]::new);

        OrderUserRePVO orderUserRePVO = new OrderUserRePVO();
        orderUserRePVO.setOrgName(orgs)
                .setUserName(username)
                .setPhone(phone)
                .setUserNo(userNo);

        return R.ok(orderUserRePVO);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 手机端 : 查看统计信息
     * @author: lc
     * @date: 2021/5/7 10:43
     * @params [userId]
     */
    @Override
    public R showStatistics(String month) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        // 获取月份
        String mon = StrUtil.subWithLength(month, 5, 2);

        // 获取用户每天最大
        LambdaQueryWrapper<OrderUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(OrderUser::getLearnTotalTime, OrderUser::getOrderCount)
                .eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(queryWrapper);

        // 若预约人员为空
        if (Objects.isNull(orderUser)) {
            throw BusinessException.of("该用户暂无预约记录");
        }

        // 查询当月预约次数
        Integer monthCount = orderSeatListMapper.getMonthCount(month, userId);

        // 查询短租每天的学习时间
        List<StatisticsLearnTimeDTO> shortDayLearnTime = orderSeatListMapper.getShortDayLearnTime(month, userId);
        Map<Integer, Integer> shortCollect = shortDayLearnTime.stream().collect(Collectors.toMap(el -> el.getDay(), el -> el.getLearnTime()));

        // 查询长租每天的学习时间
        List<StatisticsLearnTimeDTO> longDayLearnTime = orderSeatListMapper.getLongDayLearnTime(month, userId);
        Map<Integer, Double> longCollect = longDayLearnTime.stream().collect(Collectors.toMap(el -> el.getDay(), el -> Convert.toDouble(el.getLearnTime())));

        // 查询拼团每天的学习时间
        List<StatisticsLearnTimeDTO> groupDayLearnTime = orderSeatListMapper.getGroupDayLearnTime(month, userId);
        Map<Integer, Double> groupCollect = groupDayLearnTime.stream().collect(Collectors.toMap(el -> el.getDay(), el -> Convert.toDouble(el.getLearnTime())));

        // 按月份统计每天数据
        Integer monthDay = getMonthDay(month);

        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        // 统计每天学习时间
        LinkedHashMap<String, Double> hashMap = Maps.newLinkedHashMap();
        Double longestMonthTime = 0.0;
        Double learnMonthTime = 0.0;
        for (Integer i = 1; i <= monthDay; i++) {
            Double learTime = 0.0;

            //若短租map有该天数据，加上学习时间
            if (shortCollect.containsKey(i)) {
                Integer onceLearnTime = shortCollect.get(i);
                learTime += onceLearnTime;
                longestMonthTime = longestMonthTime > onceLearnTime ? longestMonthTime : onceLearnTime;
                learnMonthTime += onceLearnTime;
            }

            //若长租map有该天数据，计算当天学习时间,月学习时间，单次最长学习时间
            if (longCollect.containsKey(i)) {
                Double onceLearnTime = longCollect.get(i);
                learTime += onceLearnTime;
                longestMonthTime = longestMonthTime > onceLearnTime ? longestMonthTime : onceLearnTime;
                learnMonthTime += onceLearnTime;
            }

            //若拼团map有该天数据，计算当天学习时间,月学习时间，单次最长学习时间
            if (groupCollect.containsKey(i)) {
                Double onceLearnTime = groupCollect.get(i);
                learTime += onceLearnTime;
                longestMonthTime = longestMonthTime > onceLearnTime ? longestMonthTime : onceLearnTime;
                learnMonthTime += onceLearnTime;
            }

            // 计算每天学习时长
            Double learnTime = Convert.toDouble(decimalFormat.format(learTime / 3600.0));
            DecimalFormat decimal = new DecimalFormat("00");
            hashMap.put(mon + "-" + decimal.format(i), learnTime);
        }

        // 查看本月进入黑名单次数，违约次数
        Integer violateCount = orderSeatListMapper.getViolateCount(userId, month);
        Integer inBLCount = orderSeatListMapper.getInBLCount(userId, month);


        // 将数据放入VO
        StatisticsLearnRepVO statisticsLearnRepVO = new StatisticsLearnRepVO();

        statisticsLearnRepVO.setLearnTotalTime(decimalFormat.format(orderUser.getLearnTotalTime() / 3600.0))
                .setOrderCount(orderUser.getOrderCount())
                .setDayLearnTime(hashMap)
                .setOrderMonthCount(monthCount)
                .setUseCount(monthCount - violateCount)
                .setViolateCount(violateCount)
                .setInBlCount(inBLCount)
                .setLongestMonthTime(decimalFormat.format(longestMonthTime / 3600.0))
                .setLearnMonthTime(decimalFormat.format(learnMonthTime / 3600.0));
        return R.ok(statisticsLearnRepVO);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 查看打卡界面信息
     * @author: lc
     * @date: 2021/4/23 11:07
     * @params [showClockReqVO]
     */
    @Override
    public R showClockInfo(ShowClockReqVO showClockReqVO) {

        // 查看订单相关信息
        ShowClockRepVO clockInfo = orderSeatListMapper.getClockInfo(showClockReqVO);

        // 若订单类型为短租，计算订单时长
        if (clockInfo.getOrderType().equals(OrderConstant.OrderType.SHORT_RENT)) {
            try {
                String timeRange = twoTimeDiffer(clockInfo.getOrderStartTime(), clockInfo.getOrderEndTime());
                clockInfo.setShortOrderTime(timeRange + "h");
            } catch (ParseException e) {
                throw BusinessException.of("时间类型转换失败");
            }

            // 若订单状态为使用中，判断是否显示可暂离
            if (clockInfo.getListState() == OrderConstant.listState.IN_USE) {
                Integer awayLimitTime = clockInfo.getAwayLimitTime();
                // 计算当前时间和预约结束时间的差值是否大于暂离最大时间，大于则显示暂离
                try {
                    Date parse = DatePattern.NORM_DATETIME_FORMAT.parse(clockInfo.getOrderEndTime());
                    long l = DateUtil.betweenMs(new Date(), parse) / (1000 * 60);
                    if (l > awayLimitTime) {
                        clockInfo.setIsAllowAway(1);
                    } else {
                        clockInfo.setIsAllowAway(0);
                    }
                } catch (ParseException e) {
                    throw BusinessException.of("时间类型转换失败");
                }
            }
        }
        // 若订单为长租，查看计算总时长
        if (clockInfo.getOrderType().equals(OrderConstant.OrderType.LONG_RENT)) {
            Integer useTime = new Integer(clockInfo.getLongUseTime());
            double d = useTime / (3600.0);
            clockInfo.setLongUseTime(new DecimalFormat("#.#").format(d) + "h");
        }

        return R.ok(clockInfo);

    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 移动端：生成预约订单
     * @author: lc
     * @date: 2021/4/23 16:35
     * @params [orderSeatListVO]
     */
    @Override
    public R creatOrderList(OrderSeatListVO orderSeatListVO, SysUserDTO sysUserDTO) {

        // 获取用户信息，及当前时间
        String userId = sysUserDTO.getId();
        String tenantCode = sysUserDTO.getTenantCode();
        String username = sysUserDTO.getUsername();
        List<OrgDTO> sysOrgs = sysUserDTO.getSysOrgs();
        String orgId = sysOrgs.stream().map(OrgDTO::getId).collect(Collectors.joining("/"));
        String orgName = sysOrgs.stream().map(OrgDTO::getOrgName).collect(Collectors.joining("/"));
        Date nowDate = new Date();

        // 查看用户表是否有该用户
        Integer isUserExit = orderUserService.isUserExit(userId);

        // 计算长租订单结束时间
        if (orderSeatListVO.getOrderType().equals(OrderConstant.OrderType.LONG_RENT)) {
            Date orderStartTime = orderSeatListVO.getOrderStartTime();
            orderSeatListVO.setOrderEndTime(DateUtil.offsetDay(orderStartTime, 30));
        }

        // 获取空间信息,判断该座位是否可用
        QueryOrderSeatParam orderSeatParam = new QueryOrderSeatParam();
        orderSeatParam.setEndDate(orderSeatListVO.getOrderEndTime())
                .setStartDate(orderSeatListVO.getOrderStartTime())
                .setOrderType(orderSeatListVO.getOrderType())
                .setSeatId(orderSeatListVO.getSeatId());
        R<SeatInfoToOrder> seatInfoForOrder = spaceSeatService.getSeatInfoToOrder(orderSeatParam);
        SeatInfoToOrder seatInfo = seatInfoForOrder.getData();
        if (seatInfoForOrder.getCode() == 1) {
            throw BusinessException.of(seatInfoForOrder.getMsg());
        }
        orderSeatListVO.setBuildId(seatInfo.getBuildId()).setFloorId(seatInfo.getFloorId())
                .setRoomId(seatInfo.getRoomId());
        String buildOrgId = seatInfo.getBuildDeptId();
        OrderSeatList orderSeatList = null;


        // 生成短租订单
        if (orderSeatListVO.getOrderType().equals(OrderConstant.OrderType.SHORT_RENT)) {
            // 若当前时间大于预约开始时间，则无法预约
            if (DateUtil.compare(nowDate, orderSeatListVO.getOrderStartTime()) > 0) {
                throw BusinessException.of("预约开始时间不可早于当前时间,请重新预约");
            }

            // 若该用户在用户表有数据判断该用户是否在黑名单,用户预约次数加一
            if (isUserExit > 0) {
                orderBlrHandle(userId, buildOrgId);
            }

            //  查看每天可预约次数，取消订单次数
            OrderRuleVO orderRule = spaceSeatService.getOrderRule(orderSeatListVO.getSeatId(), null).getData();
            orderRule = handleOrderRule(orderRule);
            Integer subLimitCount = orderRule.getSubLimitCount();
            Integer subCancelCount = orderRule.getSubCancelCount();
            Integer subMinTime = orderRule.getSubMinTime();
            Integer subMaxTime = orderRule.getSubMaxTime();

            // 查看当前用户当日预约次数，没有数据则创建并赋值1,当日24点过期
            Long todayRestTime = getTodayRestTime(new Date());
            String userSubCountKey = tenantCode + ":order:userSubCount:" + userId;
            Integer orderCount = redisRepository.get(userSubCountKey, Integer.class);
            if (Objects.isNull(orderCount)) {
                redisRepository.set(userSubCountKey, 0, todayRestTime);
                orderCount = 0;
            }

            // 判断预约时长是否位于最长时间和最短时间之间
            long range = DateUtil.between(orderSeatListVO.getOrderStartTime(), orderSeatListVO.getOrderEndTime(), DateUnit.MINUTE);
            if (range < subMinTime) {
                throw BusinessException.of("预约失败,该座位允许预约最短时长为" + subMinTime + "min");
            }
            if (range > subMaxTime) {
                throw BusinessException.of("预约失败,该座位允许预约最长时长为" + subMaxTime + "min");
            }

            // 查看当前用户当日取消订单次数，没有数据则创建并赋值0
            String userCanCelCountKey = tenantCode + ":order:userCancelCount:" + userId;
            Integer cancelCount = redisRepository.get(userCanCelCountKey, Integer.class);
            if (Objects.isNull(cancelCount)) {
                redisRepository.set(userCanCelCountKey, 0, todayRestTime);
                cancelCount = 0;
            }

            // 若预约次数超过或取消订单次数，或等于限制次数，则无法生成订单
            if (subLimitCount <= orderCount && Objects.nonNull(subLimitCount)) {
                throw BusinessException.of("您今天预约次数已使用完毕,无法预约");
            }
            if (subCancelCount <= cancelCount && Objects.nonNull(cancelCount)) {
                throw BusinessException.of("您今天订单取消次数过多，无法预约");
            }

            // 查看用户有效订单的时间,确认与该订单时间段是否有重复,有重复则无法生成订单
            GetRepeatCountDTO getRepeatCountDTO = new GetRepeatCountDTO().setOrderStartTime(orderSeatListVO.getOrderStartTime())
                    .setOrderEndTime(orderSeatListVO.getOrderEndTime())
                    .setUserId(userId);
            if (orderSeatListMapper.getRepeatCount(getRepeatCountDTO) > 0) {
                throw BusinessException.of("该时间段您已有订单，无法预约");
            }

            // 初始化相关数据，生成订单
            orderSeatList = JSONObject.parseObject(JSON.toJSONString(orderSeatListVO), OrderSeatList.class);
            String listId = IdUtil.simpleUUID();
            orderSeatList.setOrderSeatId(listId).setContinueCount(0).setUserName(sysUserDTO.getUsername())
                    .setUserNo(sysUserDTO.getUserNo()).setUserPhone(sysUserDTO.getPhone()).setUserId(userId)
                    .setUserType(sysUserDTO.getUserType()).setIsLate(OrderConstant.Switch.NO)
                    .setOrderTime(nowDate).setListState(OrderConstant.listState.WAIT_ARRIVE)
                    .setBuildOrgId(buildOrgId).setIsAdvanceLeave(0)
                    .setUserOrgId(orgId).setUserOrgName(orgName)
                    .setOrderType(OrderConstant.OrderType.SHORT_RENT).setUserId(userId).setListUseTime(0).setIsComment(0)
                    .setUseDay(DateUtil.beginOfDay(orderSeatListVO.getOrderStartTime())).setDelFlag("0");



            // 生成订单编号
            String listNo = createListNo(nowDate, tenantCode);
            orderSeatList.setListNo(listNo);

            //生成订单
            orderLockService.lockCreatList(orderSeatList, tenantCode);

            // 消息队列：规定时间内未打卡，订单状态改为未签到（违约）
            OrderMQDTO orderMQDTO = new OrderMQDTO();

            // 计算延时时间
            int signLateTime = orderRule.getSignLateTime();
            long lateL = DateUtil.betweenMs(nowDate, orderSeatListVO.getOrderStartTime()) / 1000L;
            int delayLateTime = signLateTime * 60 + (int) lateL;

            // 将订单id封装，传入消息队列
            HashMap<String, Object> orderMap = Maps.newHashMap();
            orderMap.put("listId", listId);
            orderMap.put("tenantCode", tenantCode);
            orderMap.put("sysUserDTO", sysUserDTO);

            // 带入续约次数，判断是否需要改变状态
            orderMap.put("continueCount", 0);
            orderMap.put("buildOrgId", buildOrgId);
            orderMap.put("orderType", OrderConstant.OrderType.SHORT_RENT);
            OrderMQDTO orderLateMQDTO = orderMQDTO.setTime(Convert.toStr(delayLateTime * 1000)).setRoutingKey(OrderConstant.routeKey.ORDER_LATE_ROUTINGKEY).setParams(orderMap);
            rabbitMqUtils.sendOrderMsg(orderLateMQDTO);

            // 消息队列：预约结束时，订单状态改为待签退
            int waitLeaveTime = Convert.toInt(DateUtil.betweenMs(nowDate, orderSeatListVO.getOrderEndTime()) / 1000L);
            String strTime = Convert.toStr(waitLeaveTime * 1000);
            OrderMQDTO orderWaitLeaveMQDTO = orderMQDTO.setTime(strTime).setRoutingKey(OrderConstant.routeKey.ORDER_WAIT_LEAVE_ROUTINGKEY)
                    .setParams(orderMap);
            rabbitMqUtils.sendOrderMsg(orderWaitLeaveMQDTO);

            // 消息队列：规定时间内未签退，订单状态改为未签退（违约）
            int signLeaveLimitTime = orderRule.getSignLeaveLimitTime();
            // 计算延时时间
            long leaveL = DateUtil.betweenMs(nowDate, orderSeatListVO.getOrderEndTime()) / 1000L;
            int delayLeaveTime = Convert.toInt(signLeaveLimitTime * 60 + (int) leaveL);
            // 将订单id封装，传入消息队列
//            OrderMQDTO orderLeaveMQDTO = orderMQDTO.setTime(Convert.toStr(delayLeaveTime * 1000)).setRoutingKey(OrderConstant.routeKey.ORDER_LEAVE_ROUTINGKEY).setParams(orderMap);
            OrderMQDTO orderLeaveMQDTO = orderMQDTO.setTime(Convert.toStr(30 * 1000)).setRoutingKey(OrderConstant.routeKey.ORDER_LEAVE_ROUTINGKEY).setParams(orderMap);
            rabbitMqUtils.sendOrderMsg(orderLeaveMQDTO);

            //可签到时间点通知
            Long nowToStart = DateUtil.betweenMs(nowDate, orderSeatList.getOrderStartTime()) / 1000L;
            Integer signAdvanceTime = null != orderRule.getSignAdvanceTime() ? orderRule.getSignAdvanceTime() : 30;
            Long noticeTime = nowToStart - signAdvanceTime * 60;
            //定位提前30分钟通知
//            Long noticeTime = nowToStart - 1800;
            //提醒打卡延时队列
            if (noticeTime > 0) {
                OrderMQDTO noticeSignDTO = orderMQDTO.setTime(Convert.toStr(noticeTime * 1000)).setRoutingKey(OrderConstant.routeKey.CLOCK_REMINDER_ROUTINGKEY).setParams(orderMap);
                rabbitMqUtils.sendOrderMsg(noticeSignDTO);
            }

            //缓存内用户当日预约次数加一
            redisRepository.increasing(userSubCountKey, 1);

        }

        // 生成长租订单
        if (orderSeatListVO.getOrderType().equals(OrderConstant.OrderType.LONG_RENT)) {
            // 若当前时间大于预约开始时间，则无法预约
            if (DateUtil.compare(DateUtil.beginOfDay(nowDate), orderSeatListVO.getOrderStartTime()) > 0) {
                throw BusinessException.of("预约开始时间不可早于当天日期,请重新预约");
            }

            // 查看该用户是否已有长租订单
            LambdaQueryWrapper<OrderSeatList> longQueryWrapper = new LambdaQueryWrapper<>();
            longQueryWrapper.eq(OrderSeatList::getUserId, userId).eq(OrderSeatList::getOrderType, OrderConstant.OrderType.LONG_RENT)
                    .in(OrderSeatList::getListState, 1, 2);

            if (orderSeatListMapper.selectCount(longQueryWrapper) > 0) {
                throw BusinessException.of("您已有长租预约订单,不可重复预约");
            }

            // 获取长租订单阈值(h)
            if (seatInfo.getIsAutoRenewal().equals(1)) {
                String longRentTime = Objects.isNull(seatInfo.getLongRentTime()) ? "-1" : seatInfo.getLongRentTime();
                // 将阈值转化为秒
                Float timeFloat = Convert.toFloat(longRentTime);
                orderSeatListVO.setLongRequireTime(Convert.toInt(timeFloat * 3600));
            }else {
                orderSeatListVO.setLongRequireTime(-1);
            }

            orderSeatList = JSONObject.parseObject(JSON.toJSONString(orderSeatListVO), OrderSeatList.class);
            String listId = IdUtil.simpleUUID();

            // 初始化相关数据，生成订单
            orderSeatList.setOrderSeatId(listId).setContinueCount(0).setUserName(sysUserDTO.getUsername())
                    .setUserNo(sysUserDTO.getUserNo()).setUserPhone(sysUserDTO.getPhone())
                    .setUserType(sysUserDTO.getUserType()).setIsLate(OrderConstant.Switch.NO)
                    .setOrderTime(nowDate).setListState(OrderConstant.listState.WAIT_ARRIVE).setIsComment(0)
                    .setUserOrgId(orgId).setUserOrgName(orgName)
                    .setOrderType(OrderConstant.OrderType.LONG_RENT).setUserId(userId).setIsCancelLong(OrderConstant.Switch.NO)
                    .setUseDay(DateUtil.beginOfDay(orderSeatListVO.getOrderStartTime())).setDelFlag("0");

            // 查看是否需要审批
            Integer isNeedApprove = seatInfo.getIsNeedApprove();
            List<String> approveList = seatInfo.getApproveList();
            if (isNeedApprove.equals(1)) {
                // 需要审批，将订单状态置为待审批
                orderSeatList.setListState(OrderConstant.listState.APPROVE_WAIT);

                // 插入审批数据
                InsertApproveInfoDTO insertApproveInfoDTO = new InsertApproveInfoDTO();
                insertApproveInfoDTO.setOrderListId(listId)
                        .setOrderType(OrderConstant.OrderType.LONG_RENT)
                        .setUserId(userId)
                        .setUserName(username)
                        .setApplyTime(nowDate)
                        .setReason(orderSeatListVO.getReason())
                        .setOrderStartTime(orderSeatListVO.getOrderStartTime())
                        .setOrderEndTime(orderSeatListVO.getOrderEndTime())
                        .setArea(seatInfo.getBuildName() + "-" + seatInfo.getFloorName() + "-" + seatInfo.getRoomName() + "-" + seatInfo.getSeatNum())
                        .setApprovers(approveList.stream().toArray(String[]::new));
                orderApproveService.insertApproveInfo(insertApproveInfoDTO);

            }
            // 生成订单编号
            String listNo = createListNo(nowDate, tenantCode);
            orderSeatList.setListNo(listNo);

            // 加入分布式锁，查看该作为是否已被预订，若没有则生成订单
            orderLockService.lockCreatList(orderSeatList, tenantCode);

            // 查看订单覆盖多长时间（周，月）
            Integer rangeTime = Convert.toInt(DateUtil.betweenDay(orderSeatListVO.getOrderStartTime(), orderSeatListVO.getOrderEndTime(), false));

            // 消息队列：一个月后判断该订单是否被提前取消，若没有则将订单状态改为已完成
            OrderMQDTO orderMQDTO = new OrderMQDTO();
            // 将订单id封装，传入消息队列
            HashMap<String, Object> orderMap = Maps.newHashMap();
            orderMap.put("listId", listId);
            orderMap.put("tenantCode", tenantCode);
            orderMap.put("orderSeatListVO", orderSeatListVO);
//            String time = StrUtil.toString(rangeTime * 24 * 3600 * 1000L);
            String time = StrUtil.toString(10*60*1000L);
            orderMQDTO.setRoutingKey(OrderConstant.routeKey.FINISH_LONG_ROUTINGKEY).setTime(time).setParams(orderMap);
            rabbitMqUtils.sendLongMsg(orderMQDTO);
        }

        // 获用户表获取用户预约次数，并加1
        orderUserService.addOrderCount(userId);


        // 缓存当前用户在场馆预约总次数、在房间预约总次数
        ZSetOperations<String, Object> forZSet = redisTemplate.opsForZSet();
        forZSet.incrementScore(OrderConstant.PERSON_ORDER_BUILD + userId, seatInfo.getBuildId(), 1);
        forZSet.incrementScore(OrderConstant.PERSON_ORDER_ROOM + userId, seatInfo.getRoomId(), 1);

        return R.ok(orderSeatList);
    }

    /**
     * @return
     * @description: 预约时判断是否在黑名单
     * @author: lc
     * @date: 2021/6/15 16:21
     * @params
     */
    public void orderBlrHandle(String userId, String buildOrgId) {

        Integer isInBlackList = orderBlacklistService.getIsInBlackList(userId, buildOrgId);
        if (isInBlackList > 0) {
            throw BusinessException.of("用户在当前组织黑名单内，不可预约");
        }

        // 若不在黑名单内，用户表预约次数加一
        LambdaQueryWrapper<OrderUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.select(OrderUser::getOrderCount)
                .eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(userLambdaQueryWrapper);

        LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.set(OrderUser::getOrderCount, orderUser.getOrderCount() + 1)
                .eq(OrderUser::getUserId, userId);
        orderUserMapper.update(null, userLambdaUpdateWrapper);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 签到
     * @author: lc
     * @date: 2021/4/27 14:40
     * @params [orderListId]
     */
    @Override
    public R arriveOrderList(ArriveOrderReqVO arriveOrderReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 获取传参信息
        Integer clockType = arriveOrderReqVO.getClockType();
        String orderSeatId = arriveOrderReqVO.getOrderSeatId();
        Integer distance = arriveOrderReqVO.getDistance();
        Integer orderType = arriveOrderReqVO.getOrderType();

        // 获取打卡最短距离
        OrderRuleVO orderRule = getOrderRule(orderSeatId);
        int signMaxDistance = orderRule.getSignMaxDistance();

        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT)) {
            // 距离方式打卡签到
            if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
                if (signMaxDistance < distance) {
                    throw BusinessException.of("距离过远，无法打卡");
                }
                return shortArrive(tenantCode, orderSeatId, userId);
            }

            // 扫码方式签到
            if (clockType.equals(OrderConstant.clockType.CODE)) {
                return shortArrive(tenantCode, orderSeatId, userId);
            }
        }

        // 长租打卡
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {

            // 距离方式打卡签到
            if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
                if (signMaxDistance < distance) {
                    throw BusinessException.of("距离过远，无法打卡");
                }
                return longArrive(tenantCode, orderSeatId, userId);
            }

            // 扫码方式签到
            if (clockType.equals(OrderConstant.clockType.CODE)) {
                return longArrive(tenantCode, orderSeatId, userId);
            }
        }

        return R.ok("请确认预约类型是否正确");

    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 暂离
     * @author: lc
     * @date: 2021/5/5 19:59
     * @params [awayOrderReqVO]
     */
    @Override
    public R awayOrderList(AwayOrderReqVO awayOrderReqVO) {

        // 获取传参数据
        Integer clockType = awayOrderReqVO.getClockType();
        Integer distance = awayOrderReqVO.getDistance();
        String orderSeatId = awayOrderReqVO.getOrderSeatId();

        // 获取座位id
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(OrderSeatList::getSeatId)
                .eq(OrderSeatList::getOrderSeatId, orderSeatId);
        String seatId = orderSeatListMapper.selectOne(queryWrapper).getSeatId();

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 获取打卡最短距离
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(seatId, null).getData();
        orderRule = handleOrderRule(orderRule);
        int signMaxDistance = orderRule.getSignMaxDistance();
        int signAwayLimitTime = orderRule.getSignAwayLimitTime();

        // 距离方式打卡暂离
        if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
            if (signMaxDistance < distance) {
                throw BusinessException.of("距离过远，无法打卡");
            }
            return shortAway(tenantCode, orderSeatId, userId, signAwayLimitTime);
        }

        // 扫码方式打卡暂离
        if (clockType.equals(OrderConstant.clockType.CODE)) {
            return shortAway(tenantCode, orderSeatId, userId, signAwayLimitTime);
        }

        throw BusinessException.of("请确认打卡方式是否正确");

    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 暂离返回
     * @author: lc
     * @date: 2021/5/5 22:10
     * @params [awayOrderReqVO]
     */
    @Override
    public R backOrderList(AwayOrderReqVO awayOrderReqVO) {

        // 获取传参数据
        Integer clockType = awayOrderReqVO.getClockType();
        Integer distance = awayOrderReqVO.getDistance();
        String orderSeatId = awayOrderReqVO.getOrderSeatId();

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();

        // 获取打卡最短距离
//        Map<Object, Object> ruleMap = redisRepository.hmget(tenantCode + ":order:orderRuleMap");
        OrderRuleVO orderRule = getOrderRule(orderSeatId);
        int signMaxDistance = orderRule.getSignMaxDistance();

        // 距离方式打卡返回
        if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
            if (signMaxDistance < distance) {
                throw BusinessException.of("距离过远，无法打卡");
            }
            return shortBack(tenantCode, orderSeatId);
        }

        // 扫码方式打卡返回
        if (clockType.equals(OrderConstant.clockType.CODE)) {
            return shortBack(tenantCode, orderSeatId);
        }

        throw BusinessException.of("请确认打卡方式是否正确");

    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 取消预约
     * @author: lc
     * @date: 2021/5/5 22:36
     * @params [cancelOrderReqVO]
     */
    @Override
    public R cancelOrderList(CancelOrderReqVO cancelOrderReqVO) {

        // 获取传参
        String orderSeatId = cancelOrderReqVO.getOrderSeatId();
        Integer orderType = cancelOrderReqVO.getOrderType();

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 缓存获取可提前签到时间
//        Map<Object, Object> ruleMap = redisRepository.hmget(tenantCode + ":order:orderRuleMap");
        OrderRuleVO orderRule = getOrderRule(orderSeatId);
        int signAdvanceTime = orderRule.getSignAdvanceTime();
        Date nowDate = new Date();

        // 取消短租订单
        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT)) {
            // 生成分布式锁key
            String lockKey = tenantCode + ":order:back-key-lock:" + orderSeatId;
            LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            RLock lock = redisson.getLock(lockKey);
            lock.lock();
            try {
                // 若订单状态不为待签到，则不可取消预约(查看订单时间是否在可签到时间之前)
                queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderStartTime)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
                Integer listState = orderSeatList.getListState();

                // 获取可打卡时间
                Date clockDate = DateUtil.offsetSecond(orderSeatList.getOrderStartTime(), -(signAdvanceTime * 60)).toJdkDate();

                // 若在订单状态为待签到，且在可取消时间内
                if (listState.equals(OrderConstant.listState.WAIT_ARRIVE) && DateUtil.compare(clockDate, nowDate) > 0) {
                    updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.SELF_CANCEL)
                            .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                    orderSeatListMapper.update(null, updateWrapper);
                } else {
                    throw BusinessException.of("操作失败，该订单已不可取消预约");
                }
                orderSeatListMapper.update(null, updateWrapper);

                // 缓存内用户今日取消次数加一
                Long todayRestTime = getTodayRestTime(new Date());
                // 查看当前用户当日取消订单次数，没有数据则创建并赋值0
                String userCanCelCountKey = tenantCode + ":order:userCancelCount:" + userId;
                Integer cancelCount = redisRepository.get(userCanCelCountKey, Integer.class);
                if (Objects.isNull(cancelCount)) {
                    redisRepository.set(userCanCelCountKey, 0, todayRestTime);
                    cancelCount = 0;
                }
                redisRepository.increasing(userCanCelCountKey, 1);

                // 用户表取消次数加一
                orderUserService.addCancelCount(userId);


                return R.ok("订单取消成功");
            } finally {
                lock.unlock();
            }
        }

        // 取消长租订单
        if (orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            // 生成分布式锁
            String lockKey = tenantCode + ":order:back-key-lock:" + orderSeatId;
            LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            RLock lock = redisson.getLock(lockKey);
            lock.lock();
            try {
                // 查看订单详情
                queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderStartTime)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
                Date orderStartTime = orderSeatList.getOrderStartTime();
                Integer listState = orderSeatList.getListState();

                // 若订单状态为待签到，且在开始时间之前，状态置为已取消（9）
                if (listState.equals(OrderConstant.listState.WAIT_ARRIVE) && DateUtil.compare(nowDate, orderStartTime) < 0) {
                    updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.SELF_CANCEL)
                            .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                    orderSeatListMapper.update(null, updateWrapper);

                    // 用户表取消次数加一
                    orderUserService.addCancelCount(userId);

                    return R.ok("订单取消成功");
                }

                // 若订单状态为待签到，且在开始时间之后，状态置为已完成（2）
                else if (listState.equals(OrderConstant.listState.WAIT_ARRIVE) && DateUtil.compare(nowDate, orderStartTime) >= 0) {
                    updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.FINISH)
                            .set(OrderSeatList::getIsCancelLong, OrderConstant.Switch.YES)
                            .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                    orderSeatListMapper.update(null, updateWrapper);
                    return R.ok("订单取消成功");
                } else {
                    throw BusinessException.of("操作失败，该订单已不可取消预约");
                }
            } finally {
                lock.unlock();
            }
        }
        throw BusinessException.of("请确认订单类型是否正确");
    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 短租续约
     * @author: lc
     * @date: 2021/5/6 11:15
     * @params [continueOrderReqVO]
     */
    @Override
    public R continueOrderList(ContinueOrderReqVO continueOrderReqVO) {

        // 获取传参数据
        Integer clockType = continueOrderReqVO.getClockType();
        Integer distance = continueOrderReqVO.getDistance();
        String orderSeatId = continueOrderReqVO.getOrderSeatId();
        Date orderEndTime = continueOrderReqVO.getOrderEndTime();
        String seatId = continueOrderReqVO.getSeatId();
        Date orderStartTime = continueOrderReqVO.getOrderStartTime();
        Date nowDate = new Date();

        // 获取空间信息,判断该座位是否可用
        QueryOrderSeatParam orderSeatParam = new QueryOrderSeatParam();
        orderSeatParam.setEndDate(orderEndTime)
                .setStartDate(orderStartTime)
                .setOrderType(OrderConstant.OrderType.SHORT_RENT)
                .setSeatId(seatId);
        R<?> seatInfoForOrder = spaceSeatService.getSeatInfoToOrder(orderSeatParam);
        if (seatInfoForOrder.getCode() == 1) {
            throw BusinessException.of(seatInfoForOrder.getMsg());
        }

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 获取打卡最短距离
//        Map<Object, Object> ruleMap = redisRepository.hmget(tenantCode + ":order:orderRuleMap");
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(seatId, null).getData();
        orderRule = handleOrderRule(orderRule);
        int signMaxDistance = orderRule.getSignMaxDistance();

        // 查看用户有效订单的时间,确认与该订单时间段是否有重复,有重复则无法生成订单
        GetRepeatCountDTO getRepeatCountDTO = new GetRepeatCountDTO();
        getRepeatCountDTO.setUserId(userId)
                .setOrderStartTime(orderStartTime)
                .setOrderEndTime(orderEndTime);
        if (orderSeatListMapper.getRepeatCount(getRepeatCountDTO) > 0) {
            throw BusinessException.of("该时间段您已有订单，无法预约");
        }

        R r = null;

        // 距离方式打卡续约
        if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
            if (signMaxDistance < distance) {
                throw BusinessException.of("距离过远，无法续约");
            }
            r = orderLockService.shortContinue(tenantCode, orderSeatId, orderEndTime, seatId);
        }

        // 扫码方式打卡续约
        if (clockType.equals(OrderConstant.clockType.CODE)) {
            r = orderLockService.shortContinue(tenantCode, orderSeatId, orderEndTime, seatId);
        }

        // 消息队列：预约结束时，订单状态改为待签退
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        // 将订单id封装，传入消息队列
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("listId", orderSeatId);
        orderMap.put("tenantCode", tenantCode);
        // 带入续约次数，判断是否需要改变状态
        orderMap.put("continueCount", 1);
        int waitLeaveTime = Convert.toInt(DateUtil.betweenMs(nowDate, orderEndTime) / 1000L);
//            int waitLeaveTime = 15 * 60 * 1000;
        String strTime = Convert.toStr(waitLeaveTime * 1000);
        OrderMQDTO orderWaitLeaveMQDTO = orderMQDTO.setTime(strTime).setRoutingKey(OrderConstant.routeKey.ORDER_WAIT_LEAVE_ROUTINGKEY)
                .setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderWaitLeaveMQDTO);

        // 消息队列：规定时间内未签退，订单状态改为未签退（违约）
        int signLeaveLimitTime = orderRule.getSignLeaveLimitTime();
        // 计算延时时间
        long leaveL = DateUtil.betweenMs(nowDate, orderEndTime) / 1000L;
        int delayLeaveTime = Convert.toInt(signLeaveLimitTime * 60 + (int) leaveL);
        // 将订单id封装，传入消息队列
        OrderMQDTO orderLeaveMQDTO = orderMQDTO.setTime(Convert.toStr(delayLeaveTime * 1000)).setRoutingKey(OrderConstant.routeKey.ORDER_LEAVE_ROUTINGKEY).setParams(orderMap);
//            OrderMQDTO orderLeaveMQDTO = orderMQDTO.setTime(Convert.toStr( 60*1000)).setRoutingKey(OrderConstant.routeKey.ORDER_LEAVE_ROUTINGKEY).setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderLeaveMQDTO);

        return r;

    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 签退
     * @author: lc
     * @date: 2021/5/6 16:49
     * @params [leaveOrderReqVO]
     */
    @Override
    public R leaveOrderList(LeaveOrderReqVO leaveOrderReqVO) {

        // 获取传参信息
        Integer clockType = leaveOrderReqVO.getClockType();
        Integer distance = leaveOrderReqVO.getDistance();
        String orderSeatId = leaveOrderReqVO.getOrderSeatId();
        Integer orderType = leaveOrderReqVO.getOrderType();

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 获取打卡最短距离
//        Map<Object, Object> ruleMap = redisRepository.hmget(tenantCode + ":order:orderRuleMap");
        OrderRuleVO orderRule = getOrderRule(orderSeatId);
        int signMaxDistance = orderRule.getSignMaxDistance();

        // 短租订单签退
        if (OrderConstant.OrderType.SHORT_RENT.equals(orderType)) {
            // 距离方式打卡续约
            if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
                if (signMaxDistance < distance) {
                    throw BusinessException.of("距离过远，无法打卡");
                }
                return shortLeave(tenantCode, orderSeatId, userId);
            }

            // 扫码方式打卡续约
            if (clockType.equals(OrderConstant.clockType.CODE)) {
                return shortLeave(tenantCode, orderSeatId, userId);
            }
        }

        // 长租订单签退
        if (OrderConstant.OrderType.LONG_RENT.equals(orderType)) {
            // 距离方式打卡续约
            if (clockType.equals(OrderConstant.clockType.DISTANCE)) {
                if (signMaxDistance < distance) {
                    throw BusinessException.of("距离过远，无法打卡");
                }
                return longLeave(tenantCode, orderSeatId, userId);
            }

            // 扫码方式打卡续约
            if (clockType.equals(OrderConstant.clockType.CODE)) {
                return longLeave(tenantCode, orderSeatId, userId);
            }
        }

        throw BusinessException.of("请确认预约类型是否正确");


    }


    /**
     * @return void
     * @description: 长租签退
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    public R longLeave(String tenantCode, String orderSeatId, String userId) {
        Date nowDate = new Date();

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:leave-key-lock:" + orderSeatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        LambdaUpdateWrapper<OrderLongRentDetail> detailUpdateWrapper = new LambdaUpdateWrapper<>();
        LambdaQueryWrapper<OrderLongRentDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderUser> userUpdateWrapper = new LambdaUpdateWrapper<>();
        LambdaQueryWrapper<OrderUser> userQueryWrapper = new LambdaQueryWrapper<>();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getUseStartTime, OrderSeatList::getLongUseTime)
                    .eq(OrderSeatList::getOrderSeatId, orderSeatId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            Integer listState = orderSeatList.getListState();
            Date getUseStartTime = orderSeatList.getUseStartTime();

            // 计算订单使用时间
            Integer listUserTime = Math.round(DateUtil.betweenMs(getUseStartTime, nowDate) / 1000L);

            // 计算长租订单使用总时长
            Integer longUseTime = orderSeatList.getLongUseTime();
            longUseTime += listUserTime;

            // 若订单在使用中或待签到状态,更新订单数据
            if (listState.equals(OrderConstant.listState.IN_USE) || listState.equals(OrderConstant.listState.WAIT_ARRIVE)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.WAIT_ARRIVE)
                        .set(OrderSeatList::getListUseTime, listUserTime)
                        .set(OrderSeatList::getUseEndTime, nowDate)
                        .set(OrderSeatList::getLongUseTime, longUseTime)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                orderSeatListMapper.update(null, updateWrapper);

                // 查询长租明细表id
                detailQueryWrapper.select(OrderLongRentDetail::getOrderLongDetailId)
                        .eq(OrderLongRentDetail::getOrderSeatId, orderSeatId)
                        .orderByDesc(OrderLongRentDetail::getCreateTime)
                        .last("limit 1");
                String orderLongDetailId = orderLongRentDetailMapper.selectOne(detailQueryWrapper).getOrderLongDetailId();

                //更新长租明细表
                detailUpdateWrapper.set(OrderLongRentDetail::getUseEndTime, nowDate)
                        .set(OrderLongRentDetail::getListUseTime, listUserTime)
                        .set(OrderLongRentDetail::getUseEndTime, nowDate)
                        .set(OrderLongRentDetail::getIsLeave, OrderConstant.Switch.YES)
                        .eq(OrderLongRentDetail::getOrderLongDetailId, orderLongDetailId);
                orderLongRentDetailMapper.update(null, detailUpdateWrapper);

                // 查询用户学习总时间
                userQueryWrapper.select(OrderUser::getLearnTotalTime)
                        .eq(OrderUser::getUserId, userId);
                Integer learnTotalTime = orderUserMapper.selectOne(userQueryWrapper).getLearnTotalTime();
                learnTotalTime += listUserTime;

                // 更新用户学习总时长
                userUpdateWrapper.set(OrderUser::getLearnTotalTime, learnTotalTime)
                        .eq(OrderUser::getUserId, userId);
                orderUserMapper.update(null, userUpdateWrapper);
            } else {
                throw BusinessException.of("操作失败，该订单已不可签退");
            }
            orderSeatListMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }
        return R.ok("签退成功");
    }


    /**
     * @return void
     * @description: 短租签退
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    public R shortLeave(String tenantCode, String orderSeatId, String userId) {
        Date nowDate = new Date();

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:leave-key-lock:" + orderSeatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        LambdaQueryWrapper<OrderUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到，并判断是否迟到
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getUseStartTime, OrderSeatList::getOrderEndTime)
                    .eq(OrderSeatList::getOrderSeatId, orderSeatId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            Integer listState = orderSeatList.getListState();
            Date useStartTime = orderSeatList.getUseStartTime();
            Date orderEndTime = orderSeatList.getOrderEndTime();

            // 计算订单使用时间
            Integer listUserTime;
            if (DateUtil.compare(nowDate, orderEndTime) < 0) {
                listUserTime = Math.round(DateUtil.betweenMs(useStartTime, nowDate) / 1000L);
            } else {
                listUserTime = Math.round(DateUtil.betweenMs(useStartTime, orderEndTime) / 1000L);
            }

            // 若订单在使用中或待签到状态,更新订单数据
            if (listState.equals(OrderConstant.listState.IN_USE) || listState.equals(OrderConstant.listState.WAIT_LEAVE)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.FINISH)
                        .set(OrderSeatList::getListUseTime, listUserTime)
                        .set(OrderSeatList::getUseEndTime, nowDate)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);

                // 若订单在使用中则，订单置为早退
                if (listState.equals(OrderConstant.listState.IN_USE)) {
                    updateWrapper.set(OrderSeatList::getIsAdvanceLeave, 1);
                }
                orderSeatListMapper.update(null, updateWrapper);

                // 更新用户学习总时长
                userLambdaQueryWrapper.select(OrderUser::getLearnTotalTime)
                        .eq(OrderUser::getUserId, userId);
                OrderUser orderUser = orderUserMapper.selectOne(userLambdaQueryWrapper);
                userLambdaUpdateWrapper.set(OrderUser::getLearnTotalTime, orderUser.getLearnTotalTime() + listUserTime)
                        .eq(OrderUser::getUserId, userId);
                orderUserMapper.update(null, userLambdaUpdateWrapper);

            } else {
                throw BusinessException.of("操作失败，该订单已不可签退");
            }
            orderSeatListMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }
        return R.ok("签退成功");
    }


    /**
     * @return void
     * @description: 短租暂离返回打卡
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    public R shortBack(String tenantCode, String orderSeatId) {

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:back-key-lock:" + orderSeatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {

            // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到，并判断是否迟到
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderStartTime)
                    .eq(OrderSeatList::getOrderSeatId, orderSeatId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            Integer listState = orderSeatList.getListState();
            if (listState.equals(OrderConstant.listState.AWAY)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.IN_USE)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                orderSeatListMapper.update(null, updateWrapper);
            } else {
                throw BusinessException.of("操作失败，该订单已不可暂离返回");
            }
            orderSeatListMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }
        return R.ok("打卡成功");
    }


    /**
     * @return void
     * @description: 短租暂离打卡
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    public R shortAway(String tenantCode, String orderSeatId, String userId, Integer signAwayLimitTime) {

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:away-key-lock:" + orderSeatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        OrderSeatList orderSeatList = null;
        try {
            // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到，并判断是否迟到
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderStartTime, OrderSeatList::getBuildOrgId, OrderSeatList::getOrderType)
                    .eq(OrderSeatList::getOrderSeatId, orderSeatId);
            orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            Integer listState = orderSeatList.getListState();
            if (listState.equals(OrderConstant.listState.IN_USE)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.AWAY)
                        .set(OrderSeatList::getAwayTime, new Date())
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                orderSeatListMapper.update(null, updateWrapper);
            } else {
                throw BusinessException.of("操作失败，该订单已不可暂离");
            }
            orderSeatListMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }

        // 消息队列：规定时间内未返回，订单状态改为违约(6)
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        // 将订单id封装，传入消息队列
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("listId", orderSeatId);
        orderMap.put("tenantCode", tenantCode);
        orderMap.put("userId", userId);
        orderMap.put("buildOrgId", orderSeatList.getBuildOrgId());
        orderMap.put("orderType", orderSeatList.getOrderType());

        orderMQDTO.setRoutingKey(OrderConstant.routeKey.AWAY_BACK_ROUTINGKEY)
//                .setTime(Convert.toStr(signAwayLimitTime * 60 * 1000))
                .setTime(Convert.toStr(30 * 1000))
                .setParams(orderMap);
        log.info(signAwayLimitTime + "分钟后违规");
        rabbitMqUtils.sendOrderMsg(orderMQDTO);

        return R.ok("请于" + signAwayLimitTime + "分钟内返回,逾期您将会违约");
    }

    /**
     * @return void
     * @description: 短租签到打卡
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    public R shortArrive(String tenantCode, String orderSeatId, String userId) {
        Date nowDate = new Date();

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:arrive-key-lock:" + orderSeatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {

            // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到，并判断是否迟到
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderStartTime, OrderSeatList::getBuildOrgId)
                    .eq(OrderSeatList::getOrderSeatId, orderSeatId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);

            Integer listState = orderSeatList.getListState();
            if (listState.equals(OrderConstant.listState.WAIT_ARRIVE)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.IN_USE)
                        .set(OrderSeatList::getUseStartTime, nowDate)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                orderSeatListMapper.update(null, updateWrapper);
            } else {
                throw BusinessException.of("操作失败，该订单已不可签到");
            }

            // 若打卡时间迟到，则将迟到字段置为1
            if (DateUtil.compare(nowDate, orderSeatList.getOrderStartTime()) > 0) {
                updateWrapper.set(OrderSeatList::getIsLate, OrderConstant.Switch.YES);
                // TODO 迟到违约处理黑名单规则
                HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO()
                        .setTenantCode(tenantCode)
                        .setUserId(userId)
                        .setBuildOrgId(orderSeatList.getBuildOrgId())
                        .setViolateType(OrderConstant.violateType.BE_LATE)
                        .setViolateTime(nowDate);
                Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(handleBlacklistDTO));
                rabbitMqUtils.sendBlackListMsg(map, OrderConstant.routeKey.BLACK_LIST_ROUTINGKEY);
            }
            orderSeatListMapper.update(null, updateWrapper);
        } finally {
            lock.unlock();
        }

        // 将违约记录表里的连续未签到记录删除
        LambdaUpdateWrapper<OrderViolateDetail> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderViolateDetail::getUserId, userId)
                .eq(OrderViolateDetail::getBlacklistRuleType, OrderConstant.blackRuleType.CONTINUE_NO_ARRIVE);
        orderViolateDetailMapper.delete(wrapper);

        return R.ok("打卡成功");
    }

    /**
     * @return java.lang.String
     * @description: 生成订单编号
     * @author: lc
     * @date: 2021/5/11 14:16
     * @params [date]
     */

    public String createListNo(Date date, String tenantCode) {

        // 从缓存获取今日总预约数
        String redisKey = tenantCode + ":order:totalOrderCount";

        // 获取今天剩余秒数
        Integer restTime = Convert.toInt(DateUtil.betweenMs(date, DateUtil.endOfDay(date).toJdkDate()) / 1000l);
        Integer count = redisRepository.get(redisKey, Integer.class);
        if (Objects.isNull(count)) {
            redisRepository.set(redisKey, 1, restTime);
            count = 1;
        }

        redisRepository.increasing(redisKey, 1);

        // 拼接订单编号
        DecimalFormat decimalFormat = new DecimalFormat("000000");
        String listNo = StrUtil.toString(DatePattern.PURE_DATE_FORMAT.format(date)) + decimalFormat.format(count);
        return listNo;
    }


    /**
     * @return void
     * @description: 长租签到打卡
     * @author: lc
     * @date: 2021/4/27 16:52
     * @params [tenantCode, orderSeatId]
     */
    public R longArrive(String tenantCode, String orderSeatId, String userId) {
        Date nowDate = new Date();

        // 生成分布式锁key
        String lockKey = tenantCode + ":order:arrive-key-lock:" + orderSeatId;
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到，并判断是否迟到
            queryWrapper.select(OrderSeatList::getListState, OrderSeatList::getOrderStartTime).eq(OrderSeatList::getOrderSeatId, orderSeatId);
            OrderSeatList orderSeatList = orderSeatListMapper.selectOne(queryWrapper);
            Integer listState = orderSeatList.getListState();
            if (listState.equals(OrderConstant.listState.WAIT_ARRIVE)) {
                updateWrapper.set(OrderSeatList::getListState, OrderConstant.listState.IN_USE)
                        .set(OrderSeatList::getUseStartTime, nowDate)
                        .set(OrderSeatList::getUseEndTime, null)
                        .eq(OrderSeatList::getOrderSeatId, orderSeatId);
                orderSeatListMapper.update(null, updateWrapper);
            } else {
                throw BusinessException.of("签到失败，该订单不可签到");
            }
        } finally {
            lock.unlock();
        }

        // 生成长租明细表
        OrderLongRentDetail orderLongRentDetail = new OrderLongRentDetail();
        orderLongRentDetail.setIsLeave(0).setDelFlag("0").setUserId(userId)
                .setUseStartTime(nowDate).setOrderSeatId(orderSeatId).setListUseTime(0)
                .setUseDay(DateUtil.beginOfDay(nowDate).toJdkDate())
                .setUseStartTime(nowDate);
        orderLongRentDetailMapper.insert(orderLongRentDetail);

        return R.ok("打卡成功");
    }


    /**
     * @return java.lang.Long
     * @description: 获取今天的剩余时间（截止到24点,单位：秒）
     * @author: lc
     * @date: 2021/4/25 13:41
     * @params []
     */
    public Long getTodayRestTime(Date date) {

        // 获取当天23:59:59数据
        Date lastDate = DateUtil.endOfDay(new Date()).toJdkDate();

        // 返回当日剩余时间
        return DateUtil.betweenMs(date, lastDate) / 1000L;

    }


    /**
     * @return java.lang.String
     * @description: 秒转为小时(小数点后一位小数)
     * @author: lc
     * @date: 2021/4/22 10:11
     * @params [sec]
     */
    public String parseHour(String sec) {
        int i = Integer.parseInt(sec);
        String format = new DecimalFormat("#.#").format(i / 3600.0);
        return format + "h";
    }

    /**
     * @return java.lang.Double
     * @description:获取两时间之间的间隔时长(h)
     * @author: lc
     * @date: 2021/4/20 16:03
     * @params [timeBef, timeAft]
     */
    public String twoTimeDiffer(String timeBef, String timeAft) throws ParseException {
        Date parse = DatePattern.NORM_DATETIME_FORMAT.parse(timeAft);
        Date parse2 = DatePattern.NORM_DATETIME_FORMAT.parse(timeBef);
        long l = DateUtil.betweenMs(parse, parse2);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double a = l / (1000 * 3600.0);
        return decimalFormat.format(a);
    }

    /**
     * @return
     * @description: 根据订单id查看规则
     * @author: lc
     * @date: 2021/6/11 11:14
     * @params
     */
    public OrderRuleVO getOrderRule(String orderSeatId) {
        LambdaQueryWrapper<OrderSeatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(OrderSeatList::getSeatId)
                .eq(OrderSeatList::getOrderSeatId, orderSeatId);
        String seatId = orderSeatListMapper.selectOne(queryWrapper).getSeatId();
        OrderRuleVO orderRuleVO = spaceSeatService.getOrderRule(seatId, null).getData();
        return handleOrderRule(orderRuleVO);

    }

    /**
     * @return com.eseasky.modules.order.dto.SpaceInfoDTO
     * @description: 根据空间id查看空间信息
     * @author: lc
     * @date: 2021/4/19 9:12
     * @params [spaceInfoDTO]
     */
    @Override
    public SpaceInfoDTO getAreaInfo(SpaceInfoDTO spaceInfoDTO) {

        // 若建筑id不为空,获取楼层信息
        if (Objects.nonNull(spaceInfoDTO.getBuildId())) {
            LambdaQueryWrapper<SpaceBuild> builderWrapper = new LambdaQueryWrapper<>();
            builderWrapper.select(SpaceBuild::getBuildName, SpaceBuild::getBuildNum).eq(SpaceBuild::getBuildId, spaceInfoDTO.getBuildId());
            SpaceBuild spaceBuild = spaceBuildMapper.selectOne(builderWrapper);
            spaceInfoDTO.setBuildName(spaceBuild.getBuildName()).setBuildNum(spaceBuild.getBuildNum());
        }

        // 若楼层id不为空，获取楼层信息
        if (Objects.nonNull(spaceInfoDTO.getFloorId())) {
            LambdaQueryWrapper<SpaceFloor> floorWrapper = new LambdaQueryWrapper<>();
            floorWrapper.select(SpaceFloor::getFloorNum, SpaceFloor::getFloorName).eq(SpaceFloor::getFloorId, spaceInfoDTO.getFloorId());
            SpaceFloor spaceFloor = spaceFloorMapper.selectOne(floorWrapper);
            spaceInfoDTO.setFloorNum(spaceFloor.getFloorNum()).setFloorName(spaceFloor.getFloorName());
        }

        // 若房间id不为空,获取房间信息
        if (Objects.nonNull(spaceInfoDTO.getRoomId())) {
            LambdaQueryWrapper<SpaceRoom> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.select(SpaceRoom::getRoomNum, SpaceRoom::getRoomName).eq(SpaceRoom::getRoomId, spaceInfoDTO.getRoomId());
            SpaceRoom spaceRoom = spaceRoomMapper.selectOne(roomWrapper);
            spaceInfoDTO.setRoomName(spaceRoom.getRoomName()).setRoomNum(spaceRoom.getRoomNum());
        }

        // 若座位id不为空,获取座位信息
        if (Objects.nonNull(spaceInfoDTO.getSeatId())) {
            LambdaQueryWrapper<SpaceSeat> seatWrapper = new LambdaQueryWrapper<>();
            seatWrapper.select(SpaceSeat::getSeatNum).eq(SpaceSeat::getSeatId, spaceInfoDTO.getSeatId());
            spaceInfoDTO.setSeatNum(spaceSeatMapper.selectOne(seatWrapper).getSeatNum());
        }
        return spaceInfoDTO;
    }


    /**
     * 手机端，座位预约界面，根据时间段查询座位的预约状态
     */
    @Override
    public JSONObject getOrderedSeatByRoomId(Map<String, Object> param) {
        List<JSONObject> list = getBaseMapper().getOrderedSeatByRoomId(param);

        // 订单状态：1待签到;2使用中;3暂离（使用中）;4已完成;5未签到（违约）;6未签退（违约）;7暂离未返回（违约）;8待签退
        JSONObject result = new JSONObject();
        for (JSONObject map : list) {
            Integer listState = map.getInteger("listState");
            if (listState != null && (listState == 1 || listState == 2 || listState == 3)) { // 4,5,6,7,8表示座位已释放
                result.put(map.getString("seatId"), listState);
            }
        }
        return result;
    }


    /**
     * @return java.lang.Integer
     * @description: 获取月份天数（传参格式：yyyy-MM）
     * @author: lc
     * @date: 2021/5/7 14:10
     * @params [month]
     */
    public Integer getMonthDay(String date) {
        Integer year = Integer.parseInt(StrUtil.subWithLength(date, 0, 4));
        Integer month = Integer.parseInt(StrUtil.subWithLength(date, 5, 2));
        return DateUtil.lengthOfMonth(month, DateUtil.isLeapYear(year));
    }


    /**
     * @return void
     * @description:
     * @author: lc
     * @date: 2021/5/12 15:28
     * @params [ids, idType]  1.建筑id 2.楼层id 3.房间id 4.座位id 5.座位组id
     */
    public void manageCancel(List<String> ids, Integer idType) {
        Date nowDate = new Date();

        // 拼接座位更新条件
        HashSet<Integer> seatStateSet = Sets.newHashSet(1, 2, 3);
        LambdaUpdateWrapper<OrderSeatList> seatWrapper = new LambdaUpdateWrapper<>();
        seatWrapper.set(OrderSeatList::getListState, OrderConstant.listState.MANAGER_CANCEL)
                .set(OrderSeatList::getUpdateTime, nowDate)
                .in(OrderSeatList::getListState, seatStateSet);

        // 拼接拼团总订单查询条件,查询需要更改状态的订单id
        HashSet<Integer> groupListState = Sets.newHashSet(1, 3);
        LambdaQueryWrapper<OrderGroupList> groupListQueryWrapper = new LambdaQueryWrapper<>();
        groupListQueryWrapper.select(OrderGroupList::getOrderGroupId)
                .in(OrderGroupList::getListState, groupListState);


        // 若为建筑类型,取消楼层下所有订单置为管理员取消（10）状态
        if (SpaceConstant.SpaceType.BUILD == idType) {
            seatWrapper.in(OrderSeatList::getBuildId, ids);
            groupListQueryWrapper.in(OrderGroupList::getBuildId, ids);
        }

        // 若为楼层,取消楼层下所有订单置为管理员取消（10）状态
        if (SpaceConstant.SpaceType.FLOOR == idType) {
            seatWrapper.in(OrderSeatList::getFloorId, ids);
            groupListQueryWrapper.in(OrderGroupList::getFloorId, ids);
        }

        // 若为房间,取消房间下所有订单置为管理员取消（10）状态
        if (SpaceConstant.SpaceType.ROOM == idType) {
            seatWrapper.in(OrderSeatList::getRoomId, ids);
            groupListQueryWrapper.in(OrderGroupList::getRoomId, ids);
        }

        // 若为座位,取消座位所有订单置为管理员取消（10）状态
        if (SpaceConstant.SpaceType.SEAT == idType) {
            seatWrapper.in(OrderSeatList::getSeatId, ids);
        }

        // 若为座位组,取消座位所有订单置为管理员取消（10）状态
        if (SpaceConstant.SpaceType.SEAT == idType) {
            groupListQueryWrapper.in(OrderGroupList::getSeatGroupId, ids);
        }

        // 更新单人订单信息
        orderSeatListMapper.update(null, seatWrapper);


        // 查询需要更新的订单id
        List<OrderGroupList> orderGroupLists = orderGroupListMapper.selectList(groupListQueryWrapper);
        ArrayList<String> orderGroupIdList = orderGroupLists.stream().map(el -> el.getOrderGroupId()).collect(Collectors.toCollection(Lists::newArrayList));

        if (CollectionUtil.isEmpty(orderGroupIdList)) {
            return;
        }


        // 根据订单id更新订单和人员状态
        LambdaUpdateWrapper<OrderGroupList> orderGroupListUpdateWrapper = new LambdaUpdateWrapper<>();
        orderGroupListUpdateWrapper.set(OrderGroupList::getListState, OrderConstant.GroupListState.CANCEL)
                .in(OrderGroupList::getOrderGroupId, orderGroupIdList);
        orderGroupListMapper.update(null, orderGroupListUpdateWrapper);

        HashSet<Integer> set = Sets.newHashSet(1, 3, 4);
        LambdaUpdateWrapper<OrderGroupDetail> orderGroupUserUpdateWrapper = new LambdaUpdateWrapper<>();
        orderGroupUserUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.MANAGER_CANCEL)
                .in(OrderGroupDetail::getOrderGroupId, orderGroupIdList)
                .in(OrderGroupDetail::getUserState, set);
        orderGroupDetailMapper.update(null, orderGroupUserUpdateWrapper);


    }

    /**
     * @return
     * @description: 处理规则为空的字段
     * @author: lc
     * @date: 2021/6/23 10:08
     * @params
     */
    public OrderRuleVO handleOrderRule(OrderRuleVO orderRuleVO) {

        if (Objects.isNull(orderRuleVO)) {
            orderRuleVO = new OrderRuleVO();
        }

        // 可取消预约次数默认为3
        if (Objects.isNull(orderRuleVO.getSubCancelCount())) {
            orderRuleVO.setSubCancelCount(3);
        }

        // 允許拼团时间120min
        if (Objects.isNull(orderRuleVO.getGroupLimitTime())) {
            orderRuleVO.setGroupLimitTime(120);
        }

        // 可预约次数默认为10
        if (Objects.isNull(orderRuleVO.getSubLimitCount())) {
            orderRuleVO.setSubLimitCount(10);
        }

        // 默认不需要审批
        if (Objects.isNull(orderRuleVO.getIsNeedApprove())) {
            orderRuleVO.setIsNeedApprove(0);
        }

        // 迟到时间，默认10分钟
        if (Objects.isNull(orderRuleVO.getSignLateTime())) {
            orderRuleVO.setSignLateTime(10);
        }

        // 提前多长时间可签到
        if (Objects.isNull(orderRuleVO.getSignAdvanceTime())) {
            orderRuleVO.setSignAdvanceTime(30);
        }

        // 最大打卡距离
        if (Objects.isNull(orderRuleVO.getSignMaxDistance())) {
            orderRuleVO.setSignMaxDistance(1000);
        }

        // 延迟签退时间
        if (Objects.isNull(orderRuleVO.getSignLeaveLimitTime())) {
            orderRuleVO.setSignLeaveLimitTime(30);
        }

        // 暂离时间默认为30min
        if (Objects.isNull(orderRuleVO.getSignAwayLimitTime())) {
            orderRuleVO.setSignAwayLimitTime(30);
        }

        return orderRuleVO;
    }

}
