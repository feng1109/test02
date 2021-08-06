package com.eseasky.modules.order.service.impl;

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
import com.eseasky.modules.order.dto.GetRepeatCountDTO;
import com.eseasky.modules.order.dto.HandleBlacklistDTO;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.eseasky.modules.order.mapper.OrderGroupDetailMapper;
import com.eseasky.modules.order.mapper.OrderGroupListMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.service.OrderBlacklistService;
import com.eseasky.modules.order.service.OrderGroupListService;
import com.eseasky.modules.order.service.OrderLockService;
import com.eseasky.modules.order.service.OrderUserService;
import com.eseasky.modules.order.vo.OrderRuleVO;
import com.eseasky.modules.order.vo.request.*;
import com.eseasky.modules.order.vo.response.GroupInviteRepVO;
import com.eseasky.modules.order.vo.response.ListPageRePVO;
import com.eseasky.modules.order.vo.response.UserGroupRentListVO;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.request.QueryOrderSeatParam;
import com.eseasky.modules.space.vo.response.SeatInfoToOrder;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Service
public class OrderGroupListServiceImpl extends ServiceImpl<OrderGroupListMapper, OrderGroupList> implements OrderGroupListService {

    @Autowired
    OrderGroupListMapper orderGroupListMapper;

    @Autowired
    SpaceSeatService spaceSeatService;

    @Autowired
    OrderBlacklistService orderBlacklistService;

    @Autowired
    OrderSeatListServiceImpl orderSeatListService;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    OrderLockService orderLockService;

    @Autowired
    RabbitMqUtils rabbitMqUtils;

    @Autowired
    OrderGroupDetailMapper orderGroupDetailMapper;

    @Autowired
    OrderUserService orderUserService;



    /**
     * @return com.eseasky.common.code.utils.R
     * @description:
     * @author: lc
     * @date: 2021/6/15 13:51
     * @params [groupCreateReqVO]
     */
    @Override
    public R createOrder(GroupCreateReqVO groupCreateReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        String tenantCode = sysUserDTO.getTenantCode();
        String username = sysUserDTO.getUsername();
        List<OrgDTO> sysOrgs = sysUserDTO.getSysOrgs();
        String orgId = sysOrgs.stream().map(OrgDTO::getId).collect(Collectors.joining("/"));
        String orgName = sysOrgs.stream().map(OrgDTO::getOrgName).collect(Collectors.joining("/"));

        // 查看用户表是否有该用户
        Integer isUserExit = orderUserService.isUserExit(userId);

        // 获取传参信息
        Date orderStartTime = groupCreateReqVO.getOrderStartTime();
        Date orderEndTime = groupCreateReqVO.getOrderEndTime();
        String seatGroupId = groupCreateReqVO.getSeatGroupId();

        Date nowDate = new Date();
        // 获取空间信息,判断该座位是否可用
        QueryOrderSeatParam orderSeatParam = new QueryOrderSeatParam();
        orderSeatParam.setEndDate(orderEndTime)
                .setStartDate(orderStartTime)
                .setOrderType(OrderConstant.OrderType.GROUP_RENT)
                .setGroupId(seatGroupId);
        R<?> seatInfoForOrder = spaceSeatService.getSeatInfoToOrder(orderSeatParam);
        SeatInfoToOrder seatInfo = (SeatInfoToOrder) seatInfoForOrder.getData();
        if (seatInfoForOrder.getCode() == 1) {
            throw BusinessException.of(seatInfoForOrder.getMsg());
        }
        String buildOrgId = seatInfo.getBuildDeptId();

        // 判断用户是否在该组织黑名单类
        Integer isInBlackList = orderBlacklistService.getIsInBlackList(userId, buildOrgId);
        if (isInBlackList > 0) {
            throw BusinessException.of("操作失败，用户在该组织黑名单内");
        }

        // 若当前时间大于预约开始时间，则无法预约
        if (DateUtil.compare(nowDate, orderStartTime) > 0) {
            throw BusinessException.of("预约开始时间不可早于当前时间,请重新预约");
        }

        // 获取规则
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(null, groupCreateReqVO.getSeatGroupId()).getData();
        orderRule = orderSeatListService.handleOrderRule(orderRule);
        Integer subLimitCount = orderRule.getSubLimitCount();
        Integer subCancelCount = orderRule.getSubCancelCount();
        Integer subMinTime = orderRule.getSubMinTime();
        Integer subMaxTime = orderRule.getSubMaxTime();

        // 查看当前用户当日预约次数，没有数据则创建并赋值1,当日24点过期
        Long todayRestTime = orderSeatListService.getTodayRestTime(new Date());
        String userSubCountKey = tenantCode + ":order:userSubCount:" + userId;
        Integer orderCount = redisRepository.get(userSubCountKey, Integer.class);
        if (Objects.isNull(orderCount)) {
            redisRepository.set(userSubCountKey, 0, todayRestTime);
            orderCount = 0;
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

        // 判断预约时长是否位于最长时间和最短时间之间
        long range = DateUtil.between(orderStartTime, orderEndTime, DateUnit.MINUTE);
        if (range<subMinTime){
            throw BusinessException.of("预约失败,该座位允许预约最短时长为"+subMinTime+"min");
        }
        if (range>subMaxTime){
            throw BusinessException.of("预约失败,该座位允许预约最长时长为"+subMaxTime+"min");
        }

        // 查看是否有重复订单
        GetRepeatCountDTO getRepeatCountDTO = new GetRepeatCountDTO().setUserId(userId)
                .setOrderStartTime(orderStartTime)
                .setOrderEndTime(orderEndTime);
        Integer repeatCount = orderSeatListMapper.getRepeatCount(getRepeatCountDTO);
        if ( repeatCount > 0) {
            throw BusinessException.of("该时间段您已有订单，无法预约");
        }

        // 初始化订单数据
        String listId = IdUtil.simpleUUID();
        OrderGroupList orderGroupList = JSONObject.parseObject(JSON.toJSONString(groupCreateReqVO), OrderGroupList.class);
        orderGroupList.setBuildId(seatInfo.getBuildId())
                .setFloorId(seatInfo.getFloorId())
                .setRoomId(seatInfo.getRoomId())
                .setBuildOrgId(buildOrgId)
                .setOrderGroupId(listId)
                .setUserId(userId)
                .setUserCount(1)
                .setUserOrgId(orgId)
                .setUserOrgName(orgName)
                .setOrderType(OrderConstant.OrderType.GROUP_RENT)
                .setLaunchTime(nowDate)
                .setUserName(username)
                .setListState(OrderConstant.GroupListState.GROUPING);

        // 计算拼团截止时间
        Integer subGroupAllowTime = orderRule.getGroupLimitTime() * 60;
        Integer between = Convert.toInt(DateUtil.between(nowDate, orderStartTime, DateUnit.SECOND));
        // 若距开始时间小于截止时间，获截止时间为空
        if (Objects.nonNull(subGroupAllowTime) || (subGroupAllowTime < between)) {
            subGroupAllowTime = between;
        }
        DateTime cutOffTime = DateUtil.offsetSecond(nowDate, subGroupAllowTime);
        orderGroupList.setCutOffTime(cutOffTime);

        // 生成订单编号
        String listNo = orderSeatListService.createListNo(nowDate, tenantCode);
        orderGroupList.setListNo(listNo);

        //生成订单
        orderLockService.lockCreatGroupList(orderGroupList, tenantCode);

        // 消息队列：拼团截止,订单状态改为拼团失败
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        HashMap<String, Object> mqMap = Maps.newHashMap();
        mqMap.put("tenantCode", tenantCode);
        mqMap.put("listId", listId);
        orderMQDTO.setTime(StrUtil.toString(subGroupAllowTime * 1000))
                .setRoutingKey(OrderConstant.routeKey.GROUP_OFF_ROUTINGKEY)
                .setParams(mqMap);
        rabbitMqUtils.sendOrderMsg(orderMQDTO);

        // 插入发起人数据到详情表
        OrderGroupDetail orderGroupDetail = new OrderGroupDetail();
        orderGroupDetail.setOrderGroupId(listId)
                .setUserId(userId)
                .setJoinTime(nowDate)
                .setUserName(username)
                .setOrderStartTime(orderStartTime)
                .setOrderEndTime(orderEndTime)
                .setUserState(OrderConstant.GroupUserState.GROUPING)
                .setMemberType(OrderConstant.MemberType.LAUNCH);
        orderGroupDetailMapper.insert(orderGroupDetail);

        // 用户预约次数并加1
        if (isUserExit > 0) {
            orderUserService.addOrderCount(userId);
        }

        // 缓存内用户当日预约次数加一
        redisRepository.increasing(userSubCountKey, 1);


        // 将详情id插入返回数据
        orderGroupList.setOrderGroupDetailId(orderGroupDetail.getOrderGroupDetailId());
        return R.ok(orderGroupList);

    }

    /**
     * @return
     * @description: 加入拼团
     * @author: lc
     * @date: 2021/6/17 14:00
     * @params
     */
    @Override
    public R joinOrder(GroupJoinReqVO groupJoinReqVO) {

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        // 获取传参
        String orderGroupId = groupJoinReqVO.getOrderGroupId();

        // 根据订单id获取建筑组织id
        OrderGroupList orderGroupList = orderGroupListMapper.selectById(orderGroupId);
        if (Objects.isNull(orderGroupList)){
            throw BusinessException.of("操作失败，该订单已失效");
        }

        // 查看是否有重复订单
        GetRepeatCountDTO getRepeatCountDTO = new GetRepeatCountDTO().setUserId(userId)
                .setOrderStartTime(orderGroupList.getOrderStartTime())
                .setOrderEndTime(orderGroupList.getOrderEndTime());
        Integer repeatCount = orderSeatListMapper.getRepeatCount(getRepeatCountDTO);
        if ( repeatCount > 0) {
            throw BusinessException.of("该时间段您已有订单，无法加入拼团订单");
        }

        String buildOrgId = orderGroupList.getBuildOrgId();

        // 判断用户是否在该组织黑名单类
        Integer isInBlackList = orderBlacklistService.getIsInBlackList(userId, buildOrgId);
        if (isInBlackList > 0) {
            throw BusinessException.of("操作失败，用户在该组织黑名单内");
        }

        // 向数据库插入数据
        OrderGroupDetail orderGroupDetail = (OrderGroupDetail)orderLockService.joinGroup(groupJoinReqVO, sysUserDTO).getData();

        // 将详情id插入到返回参数里
        orderGroupList.setOrderGroupDetailId(orderGroupDetail.getOrderGroupDetailId());
        return R.ok(orderGroupList);
    }

    /**
     * @return
     * @description: 拼团签到
     * @author: lc
     * @date: 2021/6/17 16:55
     * @params
     */
    @Override
    public R arriveOrder(GroupArriveReqVO groupArriveReqVO) {

        // 获取传参
        String orderGroupDetailId = groupArriveReqVO.getOrderGroupDetailId();
        Integer clockType = groupArriveReqVO.getClockType();
        Date nowDate = new Date();

        // 获取人员信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();

        // 获取人员订单信息
        OrderGroupDetail orderGroupDetail = orderGroupDetailMapper.selectById(orderGroupDetailId);
        Integer userState = orderGroupDetail.getUserState();
        String orderGroupId = orderGroupDetail.getOrderGroupId();
        Date orderStartTime = orderGroupDetail.getOrderStartTime();
        String userId = orderGroupDetail.getUserId();

        // 获取总订单信息
        OrderGroupList orderGroupList = orderGroupListMapper.selectById(orderGroupId);
        String seatGroupId = orderGroupList.getSeatGroupId();
        String buildOrgId = orderGroupList.getBuildOrgId();

        // 获取最长打卡距离
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(null, seatGroupId).getData();
        orderRule = orderSeatListService.handleOrderRule(orderRule);
        Integer signMaxDistance = orderRule.getSignMaxDistance();

        // 若订单状态不为待签到则不可签到
        if (!userState.equals(OrderConstant.GroupUserState.WAIT_ARRIVE)) {
            throw BusinessException.of("操做失败，该订单已不可签到");
        }

        // 若打卡方式为距离打卡，判断签到距离是否满足条件
        if (clockType.equals(OrderConstant.clockType.DISTANCE) && Objects.nonNull(signMaxDistance)) {
            if (signMaxDistance <= groupArriveReqVO.getDistance()) {
                throw BusinessException.of("操作失败，距离过远");
            }
        }

        // 改变订单状态为使用中，插入使用时间
        LambdaUpdateWrapper<OrderGroupDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.IN_USE)
                .set(OrderGroupDetail::getUseStartTime,nowDate)
                .eq(OrderGroupDetail::getOrderGroupDetailId, orderGroupDetailId);

        // 判断现在打卡时间是否迟到，迟到则将是否迟到字段置为1
        if (DateUtil.compare(nowDate, orderStartTime) > 0) {
            updateWrapper.set(OrderGroupDetail::getIsLate, OrderConstant.Switch.YES);

            // todo 消息队列：违规 （先判空）
            HandleBlacklistDTO handleBlacklistDTO = new HandleBlacklistDTO()
                    .setTenantCode(tenantCode)
                    .setUserId(userId)
                    .setViolateType(OrderConstant.violateType.BE_LATE)
                    .setBuildOrgId(buildOrgId)
                    .setViolateTime(nowDate);
            Map<String, Object> map = JSONObject.parseObject(JSON.toJSONString(handleBlacklistDTO));
            rabbitMqUtils.sendBlackListMsg(map, OrderConstant.routeKey.BLACK_LIST_ROUTINGKEY);
        }

        orderGroupDetailMapper.update(null, updateWrapper);
        return R.ok("签到成功");
    }

    /**
     * @return
     * @description: 签退
     * @author: lc
     * @date: 2021/6/18 9:08
     * @params
     */
    @Override
    public R leaveOrder(GroupLeaveReqVO groupLeaveReqVO) {

        // 获取传参
        String orderGroupDetailId = groupLeaveReqVO.getOrderGroupDetailId();
        Integer clockType = groupLeaveReqVO.getClockType();
        Date nowDate = new Date();

        // 获取人员信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 获取人员订单信息
        OrderGroupDetail orderGroupDetail = orderGroupDetailMapper.selectById(orderGroupDetailId);
        Integer userState = orderGroupDetail.getUserState();
        String orderGroupId = orderGroupDetail.getOrderGroupId();
        Date orderEndTime = orderGroupDetail.getOrderEndTime();
        Date useStartTime = orderGroupDetail.getUseStartTime();

        // 获取总订单信息
        OrderGroupList orderGroupList = orderGroupListMapper.selectById(orderGroupId);
        String seatGroupId = orderGroupList.getSeatGroupId();

        // 获取最长打卡距离
        OrderRuleVO orderRule = spaceSeatService.getOrderRule(null, seatGroupId).getData();
        orderRule = orderSeatListService.handleOrderRule(orderRule);
        Integer signMaxDistance = orderRule.getSignMaxDistance();

        // 若订单状态不为使用中或待签退,则不可签退
        if (!(userState.equals(OrderConstant.GroupUserState.IN_USE) ||
                userState.equals(OrderConstant.GroupUserState.WAIT_LEAVE))) {
            throw BusinessException.of("操做失败，该订单已不可签退");
        }

        // 若打卡方式为距离打卡，判断签到距离是否满足条件
        if (clockType.equals(OrderConstant.clockType.DISTANCE) && Objects.nonNull(signMaxDistance)) {
            if (signMaxDistance <= groupLeaveReqVO.getDistance()) {
                throw BusinessException.of("操作失败，距离过远");
            }
        }

        // 计算签退时间，是否提前打卡,学习时长
        Date useEndTime = nowDate;
        Integer isAdvanceLeave = 1;
        Integer learnTime = Convert.toInt(DateUtil.between(useStartTime, nowDate, DateUnit.SECOND));
        if (DateUtil.compare(nowDate, orderEndTime) > 0) {
            useEndTime = orderEndTime;
            isAdvanceLeave = 0;
            learnTime = Convert.toInt(DateUtil.between(useStartTime, orderEndTime, DateUnit.SECOND));
        }

        // 更新签退数据
        LambdaUpdateWrapper<OrderGroupDetail> detailUpdateWrapper = new LambdaUpdateWrapper<>();
        detailUpdateWrapper.set(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.FINISH)
                .set(OrderGroupDetail::getUseEndTime, useEndTime)
                .set(OrderGroupDetail::getIsAdvanceLeave, isAdvanceLeave)
                .set(OrderGroupDetail::getLearnTime, learnTime)
                .eq(OrderGroupDetail::getOrderGroupDetailId, orderGroupDetailId);
        orderGroupDetailMapper.update(null,detailUpdateWrapper);

        // 查看在使用人数，若为零，总订单状态置为已完成
        LambdaQueryWrapper<OrderGroupDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
        detailQueryWrapper.select(OrderGroupDetail::getOrderGroupDetailId)
                .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                .eq(OrderGroupDetail::getUserState, OrderConstant.GroupUserState.IN_USE);
        Integer useCount = orderGroupDetailMapper.selectCount(detailQueryWrapper);
        if (useCount == 0) {
            LambdaUpdateWrapper<OrderGroupList> listUpdateWrapper = new LambdaUpdateWrapper<>();
            listUpdateWrapper.set(OrderGroupList::getListState, OrderConstant.GroupListState.FINISH)
            .eq(OrderGroupList::getOrderGroupId,orderGroupId);
            orderGroupListMapper.update(null,listUpdateWrapper);
        }

        // 更新用户表学习时长
        orderUserService.addLearnTime(userId,learnTime);


        return R.ok("签退成功");
    }

    /**
     * @return
     * @description: 邀请拼团
     * @author: lc
     * @date: 2021/6/18 13:44
     * @params
     */
    @Override
    public R inviteGroup(String orderGroupId) {

        // 获取拼团信息
        GroupInviteRepVO inviteGroupInfo = orderGroupListMapper.getInviteGroupInfo(orderGroupId);
        String orderStartTime = inviteGroupInfo.getOrderStartTime();
        String orderEndTime = inviteGroupInfo.getOrderEndTime();

        // 拼接前端时间展示字段
        String orderTime = StrUtil.subWithLength(orderStartTime, 0, 10) + "  "
                + StrUtil.subWithLength(orderStartTime, 11, 8) + "-"
                + StrUtil.subWithLength(orderEndTime, 11, 8);
        inviteGroupInfo.setOrderTime(orderTime);

        // 查看已参加成员
        LambdaQueryWrapper<OrderGroupDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
        detailQueryWrapper.select(OrderGroupDetail::getUserName)
                .eq(OrderGroupDetail::getOrderGroupId, orderGroupId)
                .orderByAsc(OrderGroupDetail::getJoinTime);
        List<OrderGroupDetail> orderGroupDetails = orderGroupDetailMapper.selectList(detailQueryWrapper);
        String[] names = orderGroupDetails.stream().map(OrderGroupDetail::getUserName).toArray(String[]::new);
        inviteGroupInfo.setMemberName(names);

        return R.ok(inviteGroupInfo);
    }


    /**
     * @return
     * @description: 获取订单详情
     * @author: lc
     * @date: 2021/6/21 16:47
     * @params
     */
    @Override
    public R showGroupDetail(String orderGroupDetailId) {

        // 获取订单id
        OrderGroupDetail orderGroupDetail = orderGroupDetailMapper.selectById(orderGroupDetailId);
        GroupInviteRepVO listDetail = (GroupInviteRepVO) inviteGroup(orderGroupDetail.getOrderGroupId()).getData();

        // 将订单状态改为个人订单状态
        listDetail.setListState(orderGroupDetail.getUserState())
                .setIsLate(orderGroupDetail.getIsLate());

        // 插入违约/取消原因
        for (OrderConstant.GroupUserStateEnum value : OrderConstant.GroupUserStateEnum.values()) {
            if (value.getState().equals(listDetail.getListState())){
                listDetail.setRemark(value.getRemark());
            }
        }
        if ((listDetail.getListState().equals(OrderConstant.GroupUserState.FINISH) && (listDetail.getIsLate().equals(1)))){
            listDetail.setRemark("迟到打卡");
        }

        return R.ok(listDetail);
    }

    @Override
    public R showGroupRentList(UserListReqVO userListReqVO) {

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
        List<UserGroupRentListVO> userShortRent = orderGroupDetailMapper.getUserGroupRent(page, userListReqVO);
        // 计算时间
        for (UserGroupRentListVO userRentListVO : userShortRent) {
            try {
                String orderTime = orderSeatListService.twoTimeDiffer(DateUtil.formatDateTime(userRentListVO.getOrderStartTime()), DateUtil.formatDateTime(userRentListVO.getOrderEndTime()));
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


}
