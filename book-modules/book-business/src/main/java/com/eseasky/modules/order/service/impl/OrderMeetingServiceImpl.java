package com.eseasky.modules.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.dto.OrgDTO;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.*;
import com.eseasky.common.dao.SysOrgMapper;
import com.eseasky.common.dao.SysUserMapper;
import com.eseasky.common.entity.SysOrg;
import com.eseasky.common.entity.SysUser;
import com.eseasky.common.rabbitmq.dto.OrderMQDTO;
import com.eseasky.common.rabbitmq.message.RabbitMqUtils;
import com.eseasky.common.service.SysOrgService;
import com.eseasky.common.service.utils.OrgTree;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.InsertApproveInfoDTO;
import com.eseasky.modules.order.dto.OrderMeetingDetailDTO;
import com.eseasky.modules.order.dto.SysUserOrgTreeDTO;
import com.eseasky.modules.order.entity.OrderMeetingInfo;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.entity.OrderMeetingRentDetail;
import com.eseasky.modules.order.entity.OrderMeetingUser;
import com.eseasky.modules.order.mapper.OrderMeetingInfoMapper;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.mapper.OrderMeetingRentDetailMapper;
import com.eseasky.modules.order.mapper.OrderMeetingUserMapper;
import com.eseasky.modules.order.service.OrderApproveService;
import com.eseasky.modules.order.service.OrderMeetingLockService;
import com.eseasky.modules.order.service.OrderMeetingService;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.utils.OrderMeetingUtil;
import com.eseasky.modules.order.utils.OrderUtils;
import com.eseasky.modules.order.vo.OrderMeetingVO;
import com.eseasky.modules.order.vo.request.*;
import com.eseasky.modules.order.vo.response.*;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.vo.SpaceConfApproveVO;
import com.eseasky.modules.space.vo.SpaceConfSignVO;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * ????????????????????? ???????????????
 * </p>
 *
 * @author
 * @since 2021-06-08
 */
@Service
@Slf4j
public class OrderMeetingServiceImpl extends ServiceImpl<OrderMeetingListMapper, OrderMeetingList> implements OrderMeetingService {

    @Resource
    private OrderMeetingListMapper orderMeetingListMapper;

    @Resource
    private OrderMeetingInfoMapper orderMeetingInfoMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private OrderMeetingUserMapper orderMeetingUserMapper;

    @Resource
    private OrderMeetingRentDetailMapper orderMeetingRentDetailMapper;

    @Resource
    private SysOrgMapper sysOrgMapper;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private OrderMeetingLockService orderMeetingLockService;

    @Autowired
    private RabbitMqUtils rabbitMqUtils;

    @Autowired
    private OrderApproveService orderApproveService;

    @Autowired
    private Redisson redisson;

    @Autowired
    private OrderNoticeService orderNoticeService;

    @Autowired
    private SysOrgService sysOrgService;

    @Override
    @Transactional
    public R<OrderMeetingList> createOrder(OrderMeetingVO orderMeetingVO) {
        log.info("OrderMeetingServiceImpl.createOrder msg:" + JSONObject.toJSONString(orderMeetingVO));
        //????????????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        // ????????????????????????????????????
        String userId = sysUserDTO.getId();
        List<OrgDTO> sysOrgs = sysUserDTO.getSysOrgs();
        String orgId = sysOrgs.stream().map(OrgDTO::getId).collect(Collectors.joining("/"));
        String orgName = sysOrgs.stream().map(OrgDTO::getOrgName).collect(Collectors.joining("/"));

        orderMeetingVO.setUserId(userId);
        String tenantCode = sysUserDTO.getTenantCode();
        Date nowDate = new Date();

        CommonUtil.check(Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderMeetingVO.getOrderType()), "????????????????????????");
        CommonUtil.notNull(orderMeetingVO.getOrderStartTime(), "??????????????????????????????");
        CommonUtil.notNull(orderMeetingVO.getOrderEndTime(), "??????????????????????????????");
        // ?????????????????????????????????????????????????????????
        if (DateUtil.compare(DateUtil.beginOfDay(nowDate), orderMeetingVO.getOrderStartTime()) > 0) {
            throw BusinessException.of("??????????????????????????????????????????,???????????????");
        }
        // ???????????????????????????????????????
        List<Integer> stateList = Arrays.asList(OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId(), OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId());
        //????????????????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingVO.getOrderType())) {
            if (orderMeetingListMapper.getExistOrderCount(userId, orderMeetingVO.getOrderStartTime(), orderMeetingVO.getOrderEndTime(), stateList, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING) > 0) {
                throw BusinessException.of("??????????????????!");
            }
        }
        //??????????????????????????????
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingVO.getOrderType())) {
            LambdaQueryWrapper<OrderMeetingList> orderMeetingListLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderMeetingListLambdaQueryWrapper.eq(OrderMeetingList::getUserId, userId)
                    .in(OrderMeetingList::getState, stateList)
                    .eq(OrderMeetingList::getOrderType, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING);
            Integer longCount = orderMeetingListMapper.selectCount(orderMeetingListLambdaQueryWrapper);
            CommonUtil.check(longCount <= 0, "??????????????????????????????");
        }

        //????????????
        SpaceConfVO conf = orderUtils.getRoomConf(orderMeetingVO.getRoomId());
        //??????????????????
        OneBOneFOneR roomInfo = orderUtils.getRoomInfoByOrder(orderMeetingVO);
        //??????????????????
        orderUtils.checkAppointProperties(orderMeetingVO, userId, tenantCode, conf, orderMeetingVO.getOrderType());

        //??????id
        String orderNo = orderUtils.createListNo(nowDate, tenantCode);
        OrderMeetingList orderMeetingList = new OrderMeetingList();
        if (ObjectUtil.isNotNull(roomInfo)) {
            //??????????????????
            orderMeetingVO.setFloorId(roomInfo.getFloorId());
            orderMeetingVO.setBuildId(roomInfo.getBuildId());
            orderMeetingVO.setRoomId(roomInfo.getRoomId());
        }
        //??????????????????????????????
        List<String> attendMeetingPeople = orderMeetingVO.getAttendMeetingPeople();
        if (CollectionUtil.isEmpty(attendMeetingPeople)) {
            attendMeetingPeople = Arrays.asList(userId);
        } else {
            attendMeetingPeople.add(userId);
        }
        orderMeetingVO.setAttendMeetingPeople(attendMeetingPeople.stream().distinct().collect(Collectors.toList()));

        // ??????????????????
        if (orderMeetingVO.getOrderType().equals(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING)) {
            orderMeetingList = delShortMeetingOrder(orderMeetingVO, sysUserDTO, userId, tenantCode, nowDate, orderNo, conf, orgId, orgName);
        }

        // ??????????????????
        if (orderMeetingVO.getOrderType().equals(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING)) {
            orderMeetingList = dealLongMeetingOrder(orderMeetingVO, sysUserDTO, tenantCode, nowDate, orderNo, conf, orgId, orgName);
        }

        Integer isNeedApprove = null != conf.getIsNeedApprove() ? conf.getIsNeedApprove() : OrderConstant.NeedApprove.NOT_APPROVE.getCode();

        //?????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????????????????????????????,????????????????????????????????????????????????????????????????????????
        if (OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(isNeedApprove) && OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            //?????????????????????????????????????????????????????????
            batchInsertOrderMeetingInfo(orderMeetingList, attendMeetingPeople);
        }

        //???????????????????????????????????????
        if (OrderConstant.NeedApprove.NEED_APPROVE.getCode().equals(isNeedApprove)) {
            List<SpaceConfApproveVO> approveVOS = conf.getApproveList();
            List<String> approveList = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(approveVOS)) {
                approveList = approveVOS.stream().map(SpaceConfApproveVO::getUserId).collect(Collectors.toList());
            }
            String[] approvers = approveList.toArray(new String[approveList.size()]);
            InsertApproveInfoDTO insertApproveInfoDTO = OrderUtils.getInsertApproveInfoDTO(orderMeetingList, roomInfo, approvers);
            orderApproveService.insertApproveInfo(insertApproveInfoDTO);
        }

        //???????????????????????????????????????
        if (OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(isNeedApprove)) {
            orderUtils.sendNoticeToAppointmentPeople(orderMeetingList.getAttendMeetingPeople(), orderMeetingList.getOrderMeetingInfoId(), orderMeetingList);
        }

        String userSubCountKey = tenantCode + ":order:userSubCount:" + userId;
        //???????????????????????????????????????
        redisRepository.increasing(userSubCountKey, 1);
        //?????????????????????????????????????????????????????????,???????????????????????????
        orderUtils.addOrUpdateOrderUser(sysUserDTO.getId(), orderMeetingList, orgId);
        return R.ok(orderMeetingList);
    }

    @Override
    @Transactional
    public R<String> approve(String orderMeetingId, Integer state) {
        log.info("OrderMeetingServiceImpl.approve msg: orderMeetingId:" + orderMeetingId + ",state:" + state);
        CommonUtil.notNullOrEmpty(orderMeetingId, "??????id????????????!");
        CommonUtil.notNull(state, "????????????????????????!");
        CommonUtil.check(Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.APPROVE_FAILED.getAftId()).contains(state), "??????????????????!");

        LambdaQueryWrapper<OrderMeetingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(wrapper);
        if (Objects.isNull(orderMeetingList)) {
            throw BusinessException.of("???????????????????????????");
        }

        LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(OrderMeetingList::getState, state)
                .set(OrderMeetingList::getUpdateTime, new Date())
                .eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        orderMeetingListMapper.update(null, updateWrapper);
        orderMeetingList.setState(state);

        List<String> attendPeopleIds = CommonUtil.stringToList(orderMeetingList.getAttendMeetingPeople());
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state) && OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            batchInsertOrderMeetingInfo(orderMeetingList, attendPeopleIds);
        }

        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state) && OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            batchInsertOrderMeetingUser(orderMeetingList, null, attendPeopleIds);
        }

        return R.ok("??????????????????");
    }

    @Override
    @Transactional
    public R<ListWithPage<UserMeetingListRepVO>> getMeetingOrderPage(UserMeetingOrderReqVO userMeetingOrderReqVO) {
        log.info("OrderMeetingServiceImpl.getMeetingOrderPage msg: " + JSONObject.toJSONString(userMeetingOrderReqVO));
        // ??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        userMeetingOrderReqVO.setUserId(userId);

        // ??????????????????????????????
        Integer timeRange = userMeetingOrderReqVO.getTimeRange();
        if (null != timeRange && !timeRange.equals(0)) {
            Date date = DateUtil.offsetMonth(new Date(), -timeRange).toJdkDate();
            userMeetingOrderReqVO.setEndTime(date);
        }

        ListWithPage<UserMeetingListRepVO> listWithPage = new ListWithPage<>();

        //???????????????????????????????????????????????????id??????
        //?????????????????????????????????????????????????????????????????????
        LambdaQueryWrapper<OrderMeetingUser> meetingUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingUserLambdaQueryWrapper.eq(OrderMeetingUser::getUserId, userId);

        List<OrderMeetingUser> meetingUserList = orderMeetingUserMapper.selectList(meetingUserLambdaQueryWrapper);
        List<String> meetingIds = meetingUserList.stream().map(OrderMeetingUser::getOrderMeetingId).distinct().collect(Collectors.toList());
        userMeetingOrderReqVO.setMeetingIds(meetingIds);
        List<Integer> states = Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId(), OrderConstant.MeetingStateEnum.FINISH.getAftId());
        //????????????
        Integer count = orderMeetingListMapper.getCount(userMeetingOrderReqVO);
        if (count <= 0) {
            return R.ok(listWithPage);
        }

        //???????????????????????????
        List<OrderMeetingRentDetail> rentDetailList = new ArrayList<>();
        if (!CollectionUtil.isEmpty(meetingIds)) {
            LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailQueryWrapper = new LambdaQueryWrapper<>();
            rentDetailQueryWrapper.in(OrderMeetingRentDetail::getOrderMeetingId, meetingIds);
            rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getUserId, userId);
            rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailQueryWrapper);
        }

        //??????????????????
        List<UserMeetingListRepVO> dataList = orderMeetingListMapper.getDataList(userMeetingOrderReqVO);
        for (UserMeetingListRepVO userMeetingListRepVO : dataList) {
            try {
                String orderTime = OrderUtils.twoTimeDiffer(DateUtil.formatDateTime(userMeetingListRepVO.getOrderStartTime()), DateUtil.formatDateTime(userMeetingListRepVO.getOrderEndTime()));
                DecimalFormat decimalFormat = new DecimalFormat("#.#");
                Long useTime = null != userMeetingListRepVO.getUseTime() ? userMeetingListRepVO.getUseTime() : 0L;
                double a = useTime / (1000 * 3600.0);
                userMeetingListRepVO.setLongUseTime(decimalFormat.format(a));
                userMeetingListRepVO.setOrderTime(orderTime);
                userMeetingListRepVO.setStartTime(DateUtil.formatDateTime(userMeetingListRepVO.getOrderStartTime()));
                userMeetingListRepVO.setEndTime(DateUtil.formatDateTime(userMeetingListRepVO.getOrderEndTime()));
                userMeetingListRepVO.setIsAppointment(userId.equals(userMeetingListRepVO.getUserId().toString()));
                //??????????????????
                Integer state = userMeetingListRepVO.getState();

                if (states.contains(state)) {
                    Long countIn = rentDetailList.stream().filter(item -> item.getOrderMeetingInfoId().equals(userMeetingListRepVO.getOrderMeetingInfoId())).count();
                    if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state) || OrderConstant.MeetingStateEnum.IN_USE.getAftId().equals(state)) {
                        userMeetingListRepVO.setSignState(countIn <= 0 ? OrderConstant.MeetingSignStateEnum.TO_BE_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
                    } else {
                        userMeetingListRepVO.setSignState(countIn <= 0 ? OrderConstant.MeetingSignStateEnum.NOT_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
                    }
                }
            } catch (ParseException e) {
                throw BusinessException.of(e.getMessage());
            }
        }
        listWithPage.setTotal(count);
        Integer pageSize = userMeetingOrderReqVO.getPageSize();
        listWithPage.setPages((count + pageSize - 1) / pageSize);
        listWithPage.setDataList(dataList);

        return R.ok(listWithPage);
    }

    @Override
    @Transactional
    public R<OrderMeetingListDetailRepVO> getMeetingDetail(MeetingDetailReqVO orderMeetingInfoReq) {
        log.info("OrderMeetingServiceImpl.getMeetingDetail msg: " + JSONObject.toJSONString(orderMeetingInfoReq));
        // ??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        String orderMeetingId = orderMeetingInfoReq.getOrderMeetingId();
        CommonUtil.notNull(orderMeetingId, "??????id????????????");

        //??????????????????
        OrderMeetingListDetailRepVO orderMeeting = orderMeetingListMapper.getOrderDetail(orderMeetingId);

        Integer orderType = orderMeeting.getOrderType();

        Date orderStartTime = orderMeeting.getOrderStartTime();
        Date orderEndTime = orderMeeting.getOrderEndTime();
        // ????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            String time = DateUtil.formatDate(orderStartTime) + " " + DateUtil.format(orderStartTime, "HH:mm") + " ~ " + DateUtil.format(orderEndTime, "HH:mm");
            orderMeeting.setTime(time);
        }

        // ????????????????????????
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderType)) {
            String time = DateUtil.formatDate(orderStartTime) + " ~ " + DateUtil.formatDate(orderEndTime);
            orderMeeting.setTime(time);
        }

        //????????????????????????????????????????????????
        LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailQueryWrapper = new LambdaQueryWrapper<>();
        rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingId);
        rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getUserId, userId);
        List<OrderMeetingRentDetail> rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailQueryWrapper);

        /**???????????????**/
        List<String> attendPeople = new ArrayList<>();
        List<String> userIds = orderUtils.getUserIds(orderMeeting.getAttendMeetingPeopleStr(), orderMeeting.getOrderMeetingInfoId());

        List<SysUser> sysUsers = orderUtils.getSysUserListByUserIds(userIds);
        attendPeople = sysUsers.stream().map(SysUser::getUsername).collect(Collectors.toList());
        orderMeeting.setAttendMeetingPeople(attendPeople);
        List<Integer> states = Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId(), OrderConstant.MeetingStateEnum.FINISH.getAftId());
        Integer state = orderMeeting.getState();

        if (states.contains(state)) {
            Long countIn = rentDetailList.stream().filter(item -> item.getOrderMeetingInfoId().equals(orderMeeting.getOrderMeetingInfoId())).count();
            if (countIn > 0) {
                orderMeeting.setSignTime(rentDetailList.get(0).getUseStartTime());
            }
            if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state) || OrderConstant.MeetingStateEnum.IN_USE.getAftId().equals(state)) {
                orderMeeting.setSignState(countIn <= 0 ? OrderConstant.MeetingSignStateEnum.TO_BE_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
            } else {
                orderMeeting.setSignState(countIn <= 0 ? OrderConstant.MeetingSignStateEnum.NOT_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
            }
        }

        return R.ok(orderMeeting);
    }

    @Override
    @Transactional
    public R<List<MeetingClockRepVO>> showClockInfo(MeetingClockReqVO meetingClockReqVO) {
        log.info("OrderMeetingServiceImpl.showClockInfo msg: " + JSONObject.toJSONString(meetingClockReqVO));
        //??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        Long timestamp = System.currentTimeMillis();
        List<Integer> states = Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId());

        //???????????????????????????????????????????????????id??????
        LambdaQueryWrapper<OrderMeetingUser> meetingUserQueryWrapper = new LambdaQueryWrapper<>();
        meetingUserQueryWrapper.eq(OrderMeetingUser::getUserId, sysUserDTO.getId());
        List<OrderMeetingUser> orderMeetingUserList = orderMeetingUserMapper.selectList(meetingUserQueryWrapper);
        if (CollectionUtil.isEmpty(orderMeetingUserList)) {
            return R.ok(new ArrayList<>());
        }
        List<String> orderMeetingIds = orderMeetingUserList.stream().map(OrderMeetingUser::getOrderMeetingId).collect(Collectors.toList());
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        LambdaQueryWrapper<OrderMeetingList> meetingListQueryWrapper = new LambdaQueryWrapper<>();
        meetingListQueryWrapper.in(OrderMeetingList::getOrderMeetingId, orderMeetingIds);
        meetingListQueryWrapper.in(OrderMeetingList::getState, states);
        meetingListQueryWrapper.ge(OrderMeetingList::getOrderEndTime, new Date());
        List<OrderMeetingList> orderMeetingLists = orderMeetingListMapper.selectList(meetingListQueryWrapper);
        orderMeetingIds = orderMeetingLists.stream().map(OrderMeetingList::getOrderMeetingId).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(orderMeetingIds)) {
            return R.ok(new ArrayList<>());
        }

        List<MeetingClockRepVO> clockInfo = new ArrayList<>();
        //????????????
        Date beginOfDay = DateUtil.beginOfDay(new Date()).toJdkDate();
        Date endOfDay = DateUtil.endOfDay(new Date()).toJdkDate();
        //?????????????????????
        List<MeetingClockRepVO> orderMeetingListShort = orderMeetingListMapper.getShortClockInfoByDate(orderMeetingIds, beginOfDay, endOfDay, states, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);

        //????????????????????????????????????????????????
        if (CollectionUtil.isEmpty(orderMeetingListShort)) {
            Date shortLatestDate = orderMeetingListMapper.getShortLatestDate(orderMeetingIds, states, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);
            if (null != shortLatestDate) {
                Date beginShortLatestOfDay = DateUtil.beginOfDay(shortLatestDate).toJdkDate();
                Date endShortLatestOfDay = DateUtil.endOfDay(shortLatestDate).toJdkDate();
                orderMeetingListShort = orderMeetingListMapper.getShortClockInfoByDate(orderMeetingIds, beginShortLatestOfDay, endShortLatestOfDay, states, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);
            }
        }
        //?????????????????????
        List<MeetingClockRepVO> orderMeetingListLong = orderMeetingListMapper.getLongClockInfoByDate(orderMeetingIds, Arrays.asList(OrderConstant.MeetingStateEnum.IN_USE.getAftId()), OrderConstant.OrderType.LONG_RENT_ORDER_MEETING);

        clockInfo.addAll(orderMeetingListLong);
        clockInfo.addAll(orderMeetingListShort);
        //??????????????????
        if (CollectionUtil.isEmpty(clockInfo)) {
            return R.ok(clockInfo);
        }

        //???????????????????????????
        LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailQueryWrapper = new LambdaQueryWrapper<>();
        rentDetailQueryWrapper.in(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingIds);
        rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getUserId, userId);
        rentDetailQueryWrapper.isNotNull(OrderMeetingRentDetail::getOrderMeetingInfoId);
        List<OrderMeetingRentDetail> rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailQueryWrapper);

        for (MeetingClockRepVO clockRepVO : clockInfo) {
            Date orderStartTime = clockRepVO.getOrderStartTime();
            Date orderEndTime = clockRepVO.getOrderEndTime();
            Integer state = clockRepVO.getState();
            clockRepVO.setListState(state);
            try {
                //????????????
                SpaceConfVO conf = orderUtils.getRoomConf(clockRepVO.getRoomId());
                List<SpaceConfSignVO> signList = conf.getSignList();
                List<Integer> codeList;
                if (CollectionUtil.isNotEmpty(signList)) {
                    codeList = signList.stream().map(SpaceConfSignVO::getSignId).map(Integer::parseInt).collect(Collectors.toList());
                } else {
                    codeList = Arrays.asList(OrderConstant.clockType.DISTANCE);
                }

                clockRepVO.setClockType(codeList.get(0));
                clockRepVO.setStartTime(DateUtil.formatTime(orderStartTime))
                        .setEndTime(DateUtil.formatTime(orderEndTime))
                        .setStartDay(DatePattern.CHINESE_DATE_FORMAT.format(orderStartTime))
                        .setEndDay(DatePattern.CHINESE_DATE_FORMAT.format(orderEndTime));

                /** ?????? **/
                if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(clockRepVO.getOrderType())) {
                    //????????????????????????
                    clockRepVO.setOrderTimeRange(OrderUtils.twoTimeDiffer(orderStartTime, orderEndTime));

                    //???????????????????????????????????????
                    Integer signAdvanceTime = null != conf.getSignAdvanceTime() ? conf.getSignAdvanceTime() : 30;
                    long count = rentDetailList.stream().filter(item -> item.getOrderMeetingInfoId().equals(clockRepVO.getOrderMeetingInfoId())).count();
                    long actualStartTime = orderStartTime.getTime() - signAdvanceTime * 60 * 1000;
                    long actualEndTime = orderEndTime.getTime();

                    if (timestamp < actualStartTime) {
                        Date ableArriveTime = DateUtil.offsetMinute(orderStartTime, -(signAdvanceTime)).toJdkDate();
                        clockRepVO.setAbleArriveTime(ableArriveTime);
                        clockRepVO.setSignState(OrderConstant.MeetingSignStateEnum.NOT_SIGN_TIME.getCode());
                    } else if (timestamp <= actualEndTime) {
                        clockRepVO.setSignState(count <= 0 ? OrderConstant.MeetingSignStateEnum.TO_BE_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
                    } else {
                        log.error("??????????????????????????????????????????");
                    }
                }

                /** ?????? **/
                if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(clockRepVO.getOrderType())) {
                    //????????????????????????
                    Long useTime = null != clockRepVO.getUseTime() ? clockRepVO.getUseTime() : 0L;
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    double a = useTime / (1000 * 3600.0);
                    clockRepVO.setLongUseTime(decimalFormat.format(a));
                    //??????????????????
                    if (StringUtils.isBlank(clockRepVO.getOrderMeetingInfoId())) {
                        clockRepVO.setSignState(OrderConstant.MeetingSignStateEnum.NOT_SIGN_TIME.getCode());
                    } else {
                        //??????????????????????????????
                        long count = rentDetailList.stream().filter(item -> item.getOrderMeetingInfoId().equals(clockRepVO.getOrderMeetingInfoId())).count();

                        if (OrderConstant.MeetingStateEnum.IN_USE.getAftId().equals(state)) {
                            clockRepVO.setSignState(count <= 0 ? OrderConstant.MeetingSignStateEnum.TO_BE_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
                        } else {
                            log.error("???????????????????????????????????????????????????");
                        }
                    }
                }
            } catch (ParseException e) {
                throw BusinessException.of("????????????????????????");
            }
        }

        return R.ok(clockInfo);
    }

    @Override
    @Transactional
    public R<String> cancelOrder(CancelMeetingOrderReqVO cancelMeetingOrderReqVO) {
        log.info("OrderMeetingServiceImpl.cancelOrder msg: " + JSONObject.toJSONString(cancelMeetingOrderReqVO));
        // ????????????
        String id = cancelMeetingOrderReqVO.getId();

        // ??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        return orderMeetingLockService.lockCancelOrder(id, tenantCode, userId);
    }

    @Override
    @Transactional
    public R<String> arriveOrder(ArriveMeetingOrderReqVO arriveMeetingOrderReqVO) {
        log.info("OrderMeetingServiceImpl.arriveOrder msg: " + JSONObject.toJSONString(arriveMeetingOrderReqVO));
        // ??????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // ??????????????????
        Integer clockType = arriveMeetingOrderReqVO.getClockType();
        String orderMeetingId = arriveMeetingOrderReqVO.getOrderMeetingId();
        Integer distance = arriveMeetingOrderReqVO.getDistance();
        Date nowDate = new Date();


        //??????????????? orderMeeting
        LambdaQueryWrapper<OrderMeetingList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(queryWrapper);
        CommonUtil.notNull(orderMeetingList, "???????????????????????????");
        CommonUtil.check(StringUtils.isNotEmpty(orderMeetingList.getOrderMeetingInfoId()), "?????????????????????????????????????????????");
        CommonUtil.check(Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderMeetingList.getOrderType()), "????????????????????????");
        //??????????????????????????????????????????????????????????????????????????????????????????????????????
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            CommonUtil.check(Objects.equals(OrderConstant.MeetingStateEnum.IN_USE.getAftId(), orderMeetingList.getState()), "???????????????????????????????????????");
        }

        //?????????????????????
        OrderMeetingInfo latestOrderMeetingInfo = orderMeetingInfoMapper.getOrderMeetingInfoByInfoId(orderMeetingList.getOrderMeetingInfoId());
        CommonUtil.notNull(latestOrderMeetingInfo, "?????????????????????????????????????????????");
        String orderMeetingInfoId = latestOrderMeetingInfo.getOrderMeetingInfoId();

        SpaceConfVO conf = orderUtils.getRoomConf(orderMeetingList.getRoomId());
        /*List<SpaceConfSignVO> signList = conf.getSignList();
        List<Integer> codeList;
        if (CollectionUtil.isNotEmpty(signList)) {
            codeList = signList.stream().map(SpaceConfSignVO::getSignId).map(Integer::parseInt).collect(Collectors.toList());
        } else {
            codeList = Arrays.asList(OrderConstant.clockType.DISTANCE);
        }

        //??????????????????
        if (CollectionUtil.isEmpty(codeList)) {
            codeList = Arrays.asList(OrderConstant.clockType.DISTANCE);
        }
        CommonUtil.check(CollectionUtil.isNotEmpty(codeList), "???????????????????????????????????????");
        StringBuilder clockWayBuilder = new StringBuilder();
        for (Integer s : codeList) {
            if (OrderConstant.clockType.DISTANCE.equals(s)) {
                clockWayBuilder.append("???????????????");
            }
            if (OrderConstant.clockType.CODE.equals(s)) {
                clockWayBuilder.append("???????????????");
            }
        }
        String clockWayStr = clockWayBuilder.toString();
        if (clockWayStr.endsWith("???")) {
            clockWayStr = clockWayStr.substring(0, clockWayStr.length() - 1);
        }
        CommonUtil.check(StringUtils.isNotBlank(clockWayStr), "????????????");
        CommonUtil.check(codeList.contains(clockType), "??????????????? " + clockWayStr);*/

        // ????????????????????????
        int signMaxDistance = null != conf.getSignMaxDistance() ? conf.getSignMaxDistance() : 100;

        // ????????????????????????
        if (OrderConstant.clockType.DISTANCE.equals(clockType)) {
            CommonUtil.check(signMaxDistance >= distance, "???????????????????????????");
        }

        //????????????????????????
        LambdaQueryWrapper<OrderMeetingRentDetail> queryWrapperMeeting = new LambdaQueryWrapper<>();
        queryWrapperMeeting.eq(OrderMeetingRentDetail::getOrderMeetingInfoId, orderMeetingList.getOrderMeetingInfoId());
        queryWrapperMeeting.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
        queryWrapperMeeting.eq(OrderMeetingRentDetail::getUserId, userId);
        //?????????????????????????????????
        OrderMeetingRentDetail orderMeetingRentDetail = orderMeetingRentDetailMapper.selectOne(queryWrapperMeeting);
        CommonUtil.isNull(orderMeetingRentDetail, "??????????????????");

        // ??????????????????key
        String lockKey = tenantCode + ":meeting_order:arrive-key-lock:" + "-" + userId + "-" + orderMeetingInfoId;
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            //????????????????????????????????????
            if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
                //???????????????????????????????????????????????????
//                Integer signLateTime = null != conf.getSignLateTime() ? conf.getSignLateTime() : 30;
                Integer signAdvanceTime = null != conf.getSignAdvanceTime() ? conf.getSignAdvanceTime() : 30;

//                CommonUtil.check(System.currentTimeMillis() <= (latestOrderMeetingInfo.getUserStartTime().getTime() + signLateTime * 60 * 1000), "????????????" + signLateTime + "??????????????????");
                CommonUtil.check(System.currentTimeMillis() >= (latestOrderMeetingInfo.getUserStartTime().getTime() - signAdvanceTime * 60 * 1000), "??????" + signAdvanceTime + "??????????????????");

                //???????????????????????????
                if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(orderMeetingList.getState())) {
                    //??????????????????????????????orderMeeting?????????????????????
                    LambdaQueryWrapper<OrderMeetingRentDetail> queryWrapperMeetingInner = new LambdaQueryWrapper<>();
                    queryWrapperMeetingInner.eq(OrderMeetingRentDetail::getOrderMeetingInfoId, orderMeetingInfoId);
                    queryWrapperMeetingInner.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
                    Integer arriveCount = orderMeetingRentDetailMapper.selectCount(queryWrapperMeetingInner);
                    //?????????????????????
                    Integer meetingStartCount = null != conf.getMeetingStartCount() ? conf.getMeetingStartCount() : 1;
                    if (arriveCount + 1 >= meetingStartCount) {
                        LambdaUpdateWrapper<OrderMeetingList> meetingUpdateWrapper = new LambdaUpdateWrapper<>();
                        LambdaUpdateWrapper<OrderMeetingInfo> meetingInfoUpdateWrapper = new LambdaUpdateWrapper<>();
                        meetingUpdateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.IN_USE.getAftId())
                                .set(OrderMeetingList::getUseStartTime, nowDate)
                                .set(OrderMeetingList::getUpdateTime, nowDate)
                                .eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
                        meetingInfoUpdateWrapper.set(OrderMeetingInfo::getState, OrderConstant.MeetingStateEnum.IN_USE.getAftId())
                                .set(OrderMeetingInfo::getUpdateTime, nowDate)
                                .eq(OrderMeetingInfo::getOrderMeetingInfoId, orderMeetingInfoId);
                        orderMeetingListMapper.update(null, meetingUpdateWrapper);
                        orderMeetingInfoMapper.update(null, meetingInfoUpdateWrapper);
                    }
                }
            }
            //????????????????????????
            OrderMeetingRentDetail detail = OrderMeetingUtil.getOrderMeetingRentDetail(orderMeetingList.getOrderMeetingId(), orderMeetingInfoId, sysUserDTO);
            orderMeetingRentDetailMapper.insert(detail);
        } finally {
            lock.unlock();
        }
        return R.ok("????????????");
    }

    @Override
    @Transactional
    public R<ListWithPage<OrderMeetingRentDetailRepVO>> showMeetingRentDetail(MeetingClockDetailReqVO clockDetailReqVO) {
        log.info("OrderMeetingServiceImpl.showMeetingRentDetail msg: " + JSONObject.toJSONString(clockDetailReqVO));
        //????????????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        CommonUtil.notNull(clockDetailReqVO.getOrderMeetingInfoId(), "??????id????????????");
        LambdaQueryWrapper<OrderMeetingInfo> meetingInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingInfoLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingInfoId, clockDetailReqVO.getOrderMeetingInfoId());
        OrderMeetingInfo orderMeetingInfo = orderMeetingInfoMapper.selectOne(meetingInfoLambdaQueryWrapper);
        CommonUtil.notNull(orderMeetingInfo, "???????????????????????????");
        LambdaQueryWrapper<OrderMeetingList> meetingListLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingListLambdaQueryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingInfo.getOrderMeetingId());
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(meetingListLambdaQueryWrapper);
        CommonUtil.notNull(orderMeetingInfo, "???????????????????????????");

        LambdaQueryWrapper<OrderMeetingRentDetail> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        detailLambdaQueryWrapper.select(OrderMeetingRentDetail::getUserId, OrderMeetingRentDetail::getUserName, OrderMeetingRentDetail::getUseStartTime, OrderMeetingRentDetail::getUseEndTime);
        //?????????????????????????????????????????????
        if (!userId.equals(orderMeetingList.getUserId())) {
            detailLambdaQueryWrapper.eq(OrderMeetingRentDetail::getUserId, userId);
        }
        detailLambdaQueryWrapper.eq(OrderMeetingRentDetail::getOrderMeetingInfoId, clockDetailReqVO.getOrderMeetingInfoId());

        Page<OrderMeetingRentDetail> page = new Page<>(clockDetailReqVO.getPageNum(), clockDetailReqVO.getPageSize());
        Page<OrderMeetingRentDetail> orderMeetingInfoPage = orderMeetingRentDetailMapper.selectPage(page, detailLambdaQueryWrapper);

        ListWithPage<OrderMeetingRentDetailRepVO> listWithPage = new ListWithPage<>();
        listWithPage.setTotal(Integer.valueOf(String.valueOf(orderMeetingInfoPage.getTotal())));

        List<OrderMeetingRentDetailRepVO> listData = new ArrayList<>();
        for (OrderMeetingRentDetail record : orderMeetingInfoPage.getRecords()) {
            OrderMeetingRentDetailRepVO rentDetailRepVO = OrderUtils.getOrderMeetingRentDetailRepVO(record);
            listData.add(rentDetailRepVO);
        }
        listWithPage.setDataList(listData);
        listWithPage.setPages(Integer.valueOf(String.valueOf(orderMeetingInfoPage.getPages())));
        return R.ok(listWithPage);
    }

    @Override
    @Transactional
    public R<String> openOrCloseMeeting(OpenCloseMeetingReqVO openCloseMeetingReqVO) {
        log.info("OrderMeetingServiceImpl.openOrCloseMeeting msg: " + JSONObject.toJSONString(openCloseMeetingReqVO));
        //????????????????????????
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        Date nowDate = new Date();
        String orderMeetingId = openCloseMeetingReqVO.getOrderMeetingId();
        CommonUtil.notNull(orderMeetingId, "??????id????????????!");
        CommonUtil.notNull(openCloseMeetingReqVO.getState(), "??????????????????!");
        CommonUtil.check(Arrays.asList(OrderConstant.OpenCloseMeeting.OPEN_APPOINTMENT, OrderConstant.OpenCloseMeeting.CLOSE_APPOINTMENT).contains(openCloseMeetingReqVO.getState()), "??????????????????");
        //????????????
        LambdaQueryWrapper<OrderMeetingList> queryWrapper = new LambdaQueryWrapper<>();
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        queryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId).eq(OrderMeetingList::getUserId, userId);
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(queryWrapper);
        CommonUtil.notNull(orderMeetingList, "??????????????????");
        //??????????????????????????????
        CommonUtil.check(System.currentTimeMillis() >= orderMeetingList.getOrderStartTime().getTime(), "????????????????????????????????????????????????");
        Integer state = orderMeetingList.getState();
        CommonUtil.notNull(orderMeetingList, "???????????????");
        CommonUtil.check(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType()), "????????????????????????????????????");
        CommonUtil.check(Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId()).contains(state), "????????????????????????!");

        LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        //????????????????????????????????????
        //???????????? ???????????????????????????
        if (OrderConstant.OpenCloseMeeting.OPEN_APPOINTMENT.equals(openCloseMeetingReqVO.getState())) {
            CommonUtil.check(OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state), "????????????????????????????????????");
            updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.IN_USE.getAftId())
                    .set(OrderMeetingList::getUpdateTime, nowDate)
                    .eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
            orderMeetingListMapper.update(null, updateWrapper);
            orderMeetingList.setState(OrderConstant.MeetingStateEnum.IN_USE.getAftId());

            List<String> userIds = orderUtils.getUserIds(orderMeetingList.getAttendMeetingPeople(), orderMeetingList.getOrderMeetingInfoId());

            batchInsertOrderMeetingInfo(orderMeetingList, userIds);
            return R.ok("??????????????????");
        }
        //????????????????????????????????????
        //???????????? ???????????????????????????
        if (OrderConstant.OpenCloseMeeting.CLOSE_APPOINTMENT.equals(openCloseMeetingReqVO.getState())) {
            //??????????????????????????????
            LambdaQueryWrapper<OrderMeetingInfo> meetingInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            meetingInfoLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingId, orderMeetingId);
            meetingInfoLambdaQueryWrapper.orderByDesc(OrderMeetingInfo::getCreateTime);
            meetingInfoLambdaQueryWrapper.last("limit 1");
            OrderMeetingInfo orderMeetingInfo = orderMeetingInfoMapper.selectOne(meetingInfoLambdaQueryWrapper);
            CommonUtil.notNull(orderMeetingInfo, "???????????????????????????????????????");

            CommonUtil.check(OrderConstant.MeetingStateEnum.IN_USE.getAftId().equals(state), "????????????????????????????????????");
            Long longUseTime = null != orderMeetingList.getUseTime() ? orderMeetingList.getUseTime() : 0;
            long time = nowDate.getTime() - orderMeetingInfo.getUserStartTime().getTime();
            Long useTime = longUseTime + time;
            updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.TO_START.getAftId())
                    .set(OrderMeetingList::getUpdateTime, nowDate)
                    .set(OrderMeetingList::getUseTime, useTime)
                    .set(OrderMeetingList::getOrderMeetingInfoId, null)
                    .eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
            orderMeetingListMapper.update(null, updateWrapper);
            //?????????????????????????????????????????????????????????
            LambdaUpdateWrapper<OrderMeetingInfo> updateWrapperMeetingInfo = new LambdaUpdateWrapper<>();
            updateWrapperMeetingInfo.set(OrderMeetingInfo::getState, OrderConstant.MeetingStateEnum.FINISH.getAftId())
                    .set(OrderMeetingInfo::getMeetingState, OrderConstant.MeetingStateEnum.FINISH.getAftId())
                    .set(OrderMeetingInfo::getUpdateTime, nowDate)
                    .set(OrderMeetingInfo::getUseEndTime, nowDate)
                    .eq(OrderMeetingInfo::getOrderMeetingInfoId, orderMeetingInfo.getOrderMeetingInfoId())
                    .eq(OrderMeetingInfo::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
            orderMeetingInfoMapper.update(null, updateWrapperMeetingInfo);

            //???????????????????????????
            orderUtils.updateOrderUserLearnTotalTime(orderMeetingList.getUserId(), time);
            return R.ok("??????????????????");
        }
        return R.ok("????????????");
    }

    @Override
    @Transactional
    public R<ListWithPage<UserMeetingInfoRepVO>> getMeetingRecord(UserMeetingListReqVO userMeetingListReqVO) {
        log.info("OrderMeetingServiceImpl.getMeetingRecord msg: " + JSONObject.toJSONString(userMeetingListReqVO));
        String orderMeetingId = userMeetingListReqVO.getOrderMeetingId();
        CommonUtil.notNull(orderMeetingId, "??????id????????????");
        //????????????
        OrderMeetingListDetailRepVO orderMeeting = orderMeetingListMapper.getOrderDetail(orderMeetingId);
        CommonUtil.notNull(orderMeeting, "???????????????????????????");

        LambdaQueryWrapper<OrderMeetingInfo> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        detailLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingId, orderMeetingId);
        detailLambdaQueryWrapper.orderByDesc(OrderMeetingInfo::getUserStartTime);

        Page<OrderMeetingInfo> page = new Page<>(userMeetingListReqVO.getPageNum(), userMeetingListReqVO.getPageSize());
        Page<OrderMeetingInfo> orderMeetingInfoPage = orderMeetingInfoMapper.selectPage(page, detailLambdaQueryWrapper);

        ListWithPage<UserMeetingInfoRepVO> listWithPage = new ListWithPage<>();
        if (orderMeetingInfoPage.getTotal() <= 0) {
            return R.ok(listWithPage);
        }
        listWithPage.setTotal(Integer.valueOf(String.valueOf(orderMeetingInfoPage.getTotal())));
        List<OrderMeetingInfo> records = orderMeetingInfoPage.getRecords();
        //????????????????????????id??????
        List<String> orderMeetingInfoIds = records.stream().map(OrderMeetingInfo::getOrderMeetingInfoId).collect(Collectors.toList());
        //??????????????????id????????????????????????
        List<OrderMeetingRentDetail> rentDetailList = new ArrayList<>();
        if (!CollectionUtil.isEmpty(orderMeetingInfoIds)) {
            LambdaQueryWrapper<OrderMeetingRentDetail> meetingRentDetailWrapper = new LambdaQueryWrapper<>();
            meetingRentDetailWrapper.in(OrderMeetingRentDetail::getOrderMeetingInfoId, orderMeetingInfoIds);
            rentDetailList = orderMeetingRentDetailMapper.selectList(meetingRentDetailWrapper);
        }

        Map<String, Long> rentDetailCountMap = rentDetailList.stream().collect(Collectors.groupingBy(OrderMeetingRentDetail::getOrderMeetingInfoId, Collectors.counting()));

        List<UserMeetingInfoRepVO> recordsVo = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(records)) {
            for (OrderMeetingInfo meetingDetail : records) {
                UserMeetingInfoRepVO meetingListVO = OrderUtils.getUserMeetingRecordVO(meetingDetail, orderMeeting);
                Long count = rentDetailCountMap.get(meetingDetail.getOrderMeetingInfoId());
                if (null != count) {
                    meetingListVO.setArriveCount(count);
                }
                recordsVo.add(meetingListVO);
            }
        }
        listWithPage.setDataList(recordsVo);
        listWithPage.setPages(Integer.valueOf(String.valueOf(orderMeetingInfoPage.getPages())));

        return R.ok(listWithPage);
    }

    @Override
    public R<String> delSpace(List<String> idList, Integer spaceType) {
        if (CollectionUtil.isEmpty(idList)) {
            return R.error("id????????????");
        }
        if (!Arrays.asList(SpaceConstant.SpaceType.BUILD, SpaceConstant.SpaceType.FLOOR, SpaceConstant.SpaceType.ROOM).contains(spaceType)) {
            return R.error("????????????????????????");
        }
        LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
        if (SpaceConstant.SpaceType.BUILD == spaceType) {
            updateWrapper.in(OrderMeetingList::getBuildId, idList);
        }

        if (SpaceConstant.SpaceType.FLOOR == spaceType) {
            updateWrapper.in(OrderMeetingList::getFloorId, idList);
        }

        if (SpaceConstant.SpaceType.ROOM == spaceType) {
            updateWrapper.in(OrderMeetingList::getRoomId, idList);
        }
        //??????????????????????????????????????????????????????????????????????????? ????????????????????????????????????????????????
        updateWrapper.in(OrderMeetingList::getState, Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId(), OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId()));
        updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.SYSTEM_CANCEL.getAftId());
        orderMeetingListMapper.update(null, updateWrapper);
        return R.ok("????????????");
    }

    @Override
    public R<List<SysUserOrgTreeDTO>> getTreeUsers() {
        log.info("OrderMeetingServiceImpl.getMeetingRecord ");
        List<SysUserOrgTreeDTO> list = new ArrayList<>();
        //1.?????????????????????
        List<SysOrg> sysOrgList = sysOrgService.getOrgList(new SysOrg());
        OrgTree orgTree = new OrgTree(sysOrgList);
        orgTree.mergeChild(sysOrgList);
        //2.???????????????????????????????????????????????????
        List<SysUser> userList = sysUserMapper.getUserAndOrgData();
        //3.????????????????????????????????????map key:?????????value:????????????
        //?????????????????????
        List<SysUser> hasOrgUsers = userList.stream().filter(item -> ObjectUtil.isNotNull(item.getOrgId())).collect(Collectors.toList());
        Map<String, List<SysUser>> orgUserMap = hasOrgUsers.stream().collect(Collectors.groupingBy(SysUser::getOrgId));
        //4.?????????????????????
        //???????????????
        List<SysOrg> firstLevel = sysOrgList.stream().filter(item -> null == item.getPid() || "-1".equals(item.getPid())).collect(Collectors.toList());
        for (SysOrg sysOrg : firstLevel) {
            list.addAll(getTreeOrg(sysOrg, sysOrgList, orgUserMap));
        }
        //5.?????????????????????????????????
        List<SysUser> noOrgUsers = userList.stream().filter(item -> ObjectUtil.isNull(item.getOrgId())).collect(Collectors.toList());
        List<SysUserOrgTreeDTO> noOrgUserOrgDTO = noOrgUsers.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList());
        list.addAll(noOrgUserOrgDTO);

        return R.ok(list);
    }

    @Override
    public R<List<SysUserOrgTreeDTO>> getTreeUsersByIsOrgAndId(SysUserOrgTreeReqVO sysUserOrgTreeReqVO) {
        log.info("OrderMeetingServiceImpl.getMeetingRecord msg: " + JSONObject.toJSONString(sysUserOrgTreeReqVO));
        Boolean user = sysUserOrgTreeReqVO.getUser();
        String id = sysUserOrgTreeReqVO.getId();
        List<SysUserOrgTreeDTO> list = new ArrayList<>();

        if (StringUtils.isBlank(id)) { //???????????????pid???????????????
            //???????????????(??????id?????????)
            List<SysOrg> sysNoPidOrgList = sysOrgMapper.getNoPidOrg();
            OrgTree orgTree = new OrgTree(sysNoPidOrgList);
            orgTree.mergeChild(sysNoPidOrgList);
            //???????????????(??????????????????)
            List<SysUser> noOrgUserList = sysUserMapper.getNoOrgUser();
            list.addAll(sysNoPidOrgList.stream().map(this::getSysUserTreeDTOBySysOrg).collect(Collectors.toList()));
            list.addAll(noOrgUserList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList()));
            return R.ok(list);
        }
        if (ObjectUtil.isNull(user) || !user) { //??????
            if (StringUtils.isNotBlank(id)) {
                //?????????
                SysOrg sysOrg = new SysOrg();
                HashSet<String> set = new HashSet<>();
                set.add(id);
                sysOrg.setOrgIds(set);
                List<SysOrg> orgPage = sysOrgMapper.getSonOrg(sysOrg);
                list.addAll(orgPage.stream().map(this::getSysUserTreeDTOBySysOrg).collect(Collectors.toList()));
                //?????????
                Integer pageNum = sysUserOrgTreeReqVO.getPageNum();
                Integer pageSize = sysUserOrgTreeReqVO.getPageSize();
                List<SysUser> userList;
                if (null != pageNum && pageNum > 0 && null != pageSize && pageSize > 0) {
                    userList = sysUserMapper.findOrgUserPage(Stream.of(id).collect(Collectors.toSet()), (pageNum - 1) * pageSize, pageSize);
                } else {
                    userList = sysUserMapper.findOrgUserPage(Stream.of(id).collect(Collectors.toSet()), null, null);
                }
                list.addAll(userList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList()));
            }
        }

        return R.ok(list);
    }

    @Override
    public R<OrderMeetingShortDetailRepVO> getShortDetail(String orderMeetingId) {
        log.info("OrderMeetingServiceImpl.getShortDetail msg: " + orderMeetingId);
        //???????????????????????????
        OrderMeetingDetailDTO meetingDetailDTO = orderMeetingListMapper.getMeetingDetail(orderMeetingId, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);
        CommonUtil.notNull(meetingDetailDTO, "????????????????????????!");
        //??????????????????
        LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        rentDetailLambdaQueryWrapper.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingId);
        List<OrderMeetingRentDetail> rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailLambdaQueryWrapper);
        OrderMeetingShortDetailRepVO detailRepVO = OrderUtils.getShortDetailRepVoByMeetingDetailDTO(meetingDetailDTO, rentDetailList);
        return R.ok(detailRepVO);
    }

    @Override
    public R<OrderMeetingLongDetailRepVO> getLongDetail(String orderMeetingId) {
        log.info("OrderMeetingServiceImpl.getLongDetail msg: " + orderMeetingId);
        //???????????????????????????
        OrderMeetingDetailDTO meetingDetailDTO = orderMeetingListMapper.getMeetingDetail(orderMeetingId, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING);
        CommonUtil.notNull(meetingDetailDTO, "????????????????????????!");
        //???????????????????????????
        LambdaQueryWrapper<OrderMeetingInfo> meetingInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingInfoLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingId, meetingDetailDTO.getOrderMeetingId()).orderByDesc(OrderMeetingInfo::getUserStartTime);
        List<OrderMeetingInfo> orderMeetingInfoList = orderMeetingInfoMapper.selectList(meetingInfoLambdaQueryWrapper);
        //????????????????????????
        //??????????????????
        LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        rentDetailLambdaQueryWrapper.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingId);
        List<OrderMeetingRentDetail> rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailLambdaQueryWrapper);

        OrderMeetingLongDetailRepVO detailRepVO = OrderUtils.getLongDetailRepVo(meetingDetailDTO,orderMeetingInfoList,rentDetailList);

        return R.ok(detailRepVO);
    }

    public SysUserOrgTreeDTO getSysUserTreeDTOBySysUser(SysUser sysUser) {
        SysUserOrgTreeDTO sysUserOrgTreeDTO = new SysUserOrgTreeDTO();
        sysUserOrgTreeDTO.setId(sysUser.getId());
        sysUserOrgTreeDTO.setName(sysUser.getUsername());
        sysUserOrgTreeDTO.setPid("-1");
        sysUserOrgTreeDTO.setUser(true);
        sysUserOrgTreeDTO.setMemberCount(0);
        sysUserOrgTreeDTO.setChildren(Collections.emptyList());
        return sysUserOrgTreeDTO;
    }

    public SysUserOrgTreeDTO getSysUserTreeDTOBySysOrg(SysOrg sysOrg) {
        SysUserOrgTreeDTO sysUserOrgTreeDTO = new SysUserOrgTreeDTO();
        sysUserOrgTreeDTO.setId(sysOrg.getId());
        sysUserOrgTreeDTO.setName(sysOrg.getOrgName());
        sysUserOrgTreeDTO.setPid(sysOrg.getPid());
        sysUserOrgTreeDTO.setUser(false);
        sysUserOrgTreeDTO.setMemberCount(sysOrg.getUserCnt());
        sysUserOrgTreeDTO.setChildren(null);
        return sysUserOrgTreeDTO;
    }

    public List<SysUserOrgTreeDTO> getTreeOrg(SysOrg father, List<SysOrg> list, Map<String, List<SysUser>> orgUserMap) {
        ArrayList<SysUserOrgTreeDTO> result = new ArrayList<>();

        List<SysOrg> son = list.stream().filter(item -> item.getPid().equals(father.getId())).collect(Collectors.toList());
        for (SysOrg sysOrg : son) {
            ArrayList<SysUserOrgTreeDTO> children = new ArrayList<>();
            //????????????????????????
            List<SysUser> sysUserList = orgUserMap.get(sysOrg.getId());
            if (CollectionUtil.isNotEmpty(sysUserList)) {
                List<SysUserOrgTreeDTO> noOrgUserOrgDTO = sysUserList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList());
                children.addAll(noOrgUserOrgDTO);
            }
            //??????????????????????????????
            List<SysUserOrgTreeDTO> treeOrg = getTreeOrg(sysOrg, list, orgUserMap);
            children.addAll(treeOrg);
            SysUserOrgTreeDTO sysUserTreeDTOBySysOrg = getSysUserTreeDTOBySysOrg(sysOrg);
            sysUserTreeDTOBySysOrg.setChildren(children);
            result.add(sysUserTreeDTOBySysOrg);
        }

        return result;
    }

    public List<SysUserOrgTreeDTO> getTreeOrg(List<SysOrg> list, Map<String, List<SysUser>> orgUserMap) {
        ArrayList<SysUserOrgTreeDTO> result = new ArrayList<>();

        for (SysOrg sysOrg : list) {
            ArrayList<SysUserOrgTreeDTO> children = new ArrayList<>();
            //????????????????????????
            List<SysUser> sysUserList = orgUserMap.get(sysOrg.getId());
            if (CollectionUtil.isNotEmpty(sysUserList)) {
                List<SysUserOrgTreeDTO> noOrgUserOrgDTO = sysUserList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList());
                children.addAll(noOrgUserOrgDTO);
            }
            //??????????????????????????????
            List<SysOrg> childrenOrgList = list.stream().filter(item -> item.getPid().equals(sysOrg.getId())).collect(Collectors.toList());
            List<SysUserOrgTreeDTO> treeOrg = getTreeOrg(childrenOrgList, orgUserMap);
            children.addAll(treeOrg);
            SysUserOrgTreeDTO sysUserTreeDTOBySysOrg = getSysUserTreeDTOBySysOrg(sysOrg);
            sysUserTreeDTOBySysOrg.setChildren(children);
            result.add(sysUserTreeDTOBySysOrg);
        }

        return result;
    }

    public void batchInsertOrderMeetingInfo(OrderMeetingList orderMeetingList, List<String> orderMeetingPeopleIds) {
        //?????????????????????
        List<OrderMeetingInfo> detailList = new ArrayList<>();

        OrderMeetingInfo orderMeetingInfo = OrderMeetingUtil.getOrderMeetingInfo(orderMeetingList);
        detailList.add(orderMeetingInfo);
        orderMeetingInfoMapper.batchInsert(detailList);
        LambdaUpdateWrapper<OrderMeetingList> orderMeetingListLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        orderMeetingListLambdaUpdateWrapper.set(OrderMeetingList::getOrderMeetingInfoId, orderMeetingInfo.getOrderMeetingInfoId()).eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
        orderMeetingListMapper.update(null, orderMeetingListLambdaUpdateWrapper);

        //?????????????????????????????????
        batchInsertOrderMeetingUser(orderMeetingList, orderMeetingInfo.getOrderMeetingInfoId(), orderMeetingPeopleIds);
    }

    /**
     * ????????????????????????????????????
     *
     * @param orderMeetingList
     * @return
     */
    private void batchInsertOrderMeetingUser(OrderMeetingList orderMeetingList, String orderMeetingInfoId, List<String> orderMeetingPeopleIds) {
        //?????????????????????????????????
        List<OrderMeetingUser> meetingUserDataList = new ArrayList<>();
        for (String userId : orderMeetingPeopleIds) {
            OrderMeetingUser orderMeetingUser = OrderMeetingUtil.getOrderMeetingUser(orderMeetingList.getOrderMeetingId(), orderMeetingInfoId, userId);
            meetingUserDataList.add(orderMeetingUser);
        }
        if (CollectionUtil.isNotEmpty(meetingUserDataList)) {
            orderMeetingUserMapper.batchInsert(meetingUserDataList);
        }
    }


    /**
     * ????????????: <br>
     * ???????????????????????????????????????
     *
     * @return
     * @Param: [orderMeetingVO, sysUserDTO, tenantCode, orderNo]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/9 11:04
     */
    private OrderMeetingList dealLongMeetingOrder(OrderMeetingVO orderMeetingVO, SysUserDTO sysUserDTO, String tenantCode, Date nowDate, String orderNo, SpaceConfVO conf, String orgId, String orgName) {
        //???VO??????entity
        OrderMeetingList orderMeetingList = OrderMeetingUtil.getOrderMeeting(orderMeetingVO, sysUserDTO, orderNo, OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(null != conf.getIsNeedApprove() ? conf.getIsNeedApprove() : OrderConstant.NeedApprove.NOT_APPROVE.getCode()), orgId, orgName);
        orderMeetingList.setUserOrgId(orgId);
        orderMeetingList.setUserOrgName(orgName);
        //????????????
        orderMeetingLockService.lockCreatOrder(orderMeetingList, tenantCode);
        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        // ?????????id???????????????????????????
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("orderMeetingId", orderMeetingList.getOrderMeetingId());
        orderMap.put("tenantCode", tenantCode);
        orderMap.put("sysUserDTO", sysUserDTO);

        String time = StrUtil.toString(DateUtil.betweenMs(nowDate, orderMeetingVO.getOrderEndTime()));
        //??????????????????
        orderMQDTO.setRoutingKey(OrderConstant.routeKey.FINISH_MEETING_ROUTINGKEY).setTime(time).setParams(orderMap);
        rabbitMqUtils.sendLongMsg(orderMQDTO);
        return orderMeetingList;
    }

    /**
     * ????????????: <br>
     * ???????????????????????????????????????
     *
     * @return
     * @Param: [orderMeetingVO, sysUserDTO, tenantCode, orderNo]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/9 11:04
     */
    private OrderMeetingList delShortMeetingOrder(OrderMeetingVO orderMeetingVO, SysUserDTO sysUserDTO, String userId, String tenantCode, Date nowDate, String orderNo, SpaceConfVO conf, String orgId, String orgName) {
        //???VO??????entity
        OrderMeetingList orderMeetingList = OrderMeetingUtil.getOrderMeeting(orderMeetingVO, sysUserDTO, orderNo, OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(null != conf.getIsNeedApprove() ? conf.getIsNeedApprove() : OrderConstant.NeedApprove.NOT_APPROVE.getCode()), orgId, orgName);
        //????????????
        orderMeetingLockService.lockCreatOrder(orderMeetingList, tenantCode);

        OrderMQDTO orderMQDTO = new OrderMQDTO();
        //????????????????????????????????????????????????????????????
        int meetingCancelTime = null != conf.getMeetingCancelCount() ? conf.getMeetingCancelCount() : 30;
        Integer signAdvanceTime = null != conf.getSignAdvanceTime() ? conf.getSignAdvanceTime() : 30;
        //??????????????????
        long lateL = DateUtil.betweenMs(nowDate, orderMeetingList.getOrderStartTime()) / 1000L;
        int delayLateTime = meetingCancelTime * 60 + (int) lateL;
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("orderMeetingId", orderMeetingList.getOrderMeetingId());
        orderMap.put("tenantCode", tenantCode);

        //?????????????????????????????????????????????????????????????????????
        OrderMQDTO orderLateMQDTO = orderMQDTO.setTime(Convert.toStr(delayLateTime * 1000)).setRoutingKey(OrderConstant.routeKey.CANCEL_MEETING_ROUTINGKEY).setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderLateMQDTO);

        //???????????????????????????????????????
        int waitLeaveTime = Convert.toInt(DateUtil.betweenMs(nowDate, orderMeetingVO.getOrderEndTime()));
        String strTime = Convert.toStr(waitLeaveTime);
        //???????????????????????????
        OrderMQDTO orderFinishMQDTO = orderMQDTO.setTime(strTime).setRoutingKey(OrderConstant.routeKey.FINISH_MEETING_ROUTINGKEY).setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderFinishMQDTO);

        //????????????????????????
        Long nowToStart = DateUtil.betweenMs(nowDate, orderMeetingList.getOrderStartTime()) / 1000L;
        Long noticeTime = nowToStart - signAdvanceTime * 60;
        //??????????????????
        if (noticeTime > 0) {
            OrderMQDTO noticeSignDTO = orderMQDTO.setTime(Convert.toStr(noticeTime * 1000)).setRoutingKey(OrderConstant.routeKey.CLOCK_REMINDER_MEETING_ROUTINGKEY).setParams(orderMap);
            rabbitMqUtils.sendOrderMsg(noticeSignDTO);
        }

        return orderMeetingList;
    }

    /**
     * ??????????????????????????????
     *
     * @param orderMeetingList
     * @return
     */
    private OrderMeetingInfo getLatestOrderMeetingInfoByOrderMeetingList(OrderMeetingList orderMeetingList) {
        OrderMeetingInfo orderMeetingInfo = new OrderMeetingInfo();
        if (ObjectUtil.isNotNull(orderMeetingList) && StringUtils.isNotBlank(orderMeetingList.getOrderMeetingInfoId())) {
            orderMeetingInfo = orderMeetingInfoMapper.getOrderMeetingInfoByInfoId(orderMeetingList.getOrderMeetingInfoId());
        }
        return orderMeetingInfo;
    }
}
