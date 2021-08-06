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
 * 会议室预约订单 服务实现类
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
        //获取操作用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        // 获取用户信息，及当前时间
        String userId = sysUserDTO.getId();
        List<OrgDTO> sysOrgs = sysUserDTO.getSysOrgs();
        String orgId = sysOrgs.stream().map(OrgDTO::getId).collect(Collectors.joining("/"));
        String orgName = sysOrgs.stream().map(OrgDTO::getOrgName).collect(Collectors.joining("/"));

        orderMeetingVO.setUserId(userId);
        String tenantCode = sysUserDTO.getTenantCode();
        Date nowDate = new Date();

        CommonUtil.check(Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderMeetingVO.getOrderType()), "不支持此订单类型");
        CommonUtil.notNull(orderMeetingVO.getOrderStartTime(), "预约开始时间不可为空");
        CommonUtil.notNull(orderMeetingVO.getOrderEndTime(), "预约结束时间不可为空");
        // 若当前时间大于预约开始时间，则无法预约
        if (DateUtil.compare(DateUtil.beginOfDay(nowDate), orderMeetingVO.getOrderStartTime()) > 0) {
            throw BusinessException.of("预约开始时间不可早于当天日期,请重新预约");
        }
        // 查看该用户是否已有长租订单
        List<Integer> stateList = Arrays.asList(OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId(), OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId());
        //短租查看同一时间重复订单
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingVO.getOrderType())) {
            if (orderMeetingListMapper.getExistOrderCount(userId, orderMeetingVO.getOrderStartTime(), orderMeetingVO.getOrderEndTime(), stateList, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING) > 0) {
                throw BusinessException.of("请勿重复预约!");
            }
        }
        //长租只能存在一个订单
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingVO.getOrderType())) {
            LambdaQueryWrapper<OrderMeetingList> orderMeetingListLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderMeetingListLambdaQueryWrapper.eq(OrderMeetingList::getUserId, userId)
                    .in(OrderMeetingList::getState, stateList)
                    .eq(OrderMeetingList::getOrderType, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING);
            Integer longCount = orderMeetingListMapper.selectCount(orderMeetingListLambdaQueryWrapper);
            CommonUtil.check(longCount <= 0, "不可预约多个长租订单");
        }

        //获取规则
        SpaceConfVO conf = orderUtils.getRoomConf(orderMeetingVO.getRoomId());
        //空间属性校验
        OneBOneFOneR roomInfo = orderUtils.getRoomInfoByOrder(orderMeetingVO);
        //预约属性校验
        orderUtils.checkAppointProperties(orderMeetingVO, userId, tenantCode, conf, orderMeetingVO.getOrderType());

        //生成id
        String orderNo = orderUtils.createListNo(nowDate, tenantCode);
        OrderMeetingList orderMeetingList = new OrderMeetingList();
        if (ObjectUtil.isNotNull(roomInfo)) {
            //设置楼层信息
            orderMeetingVO.setFloorId(roomInfo.getFloorId());
            orderMeetingVO.setBuildId(roomInfo.getBuildId());
            orderMeetingVO.setRoomId(roomInfo.getRoomId());
        }
        //将预约人添加入参会人
        List<String> attendMeetingPeople = orderMeetingVO.getAttendMeetingPeople();
        if (CollectionUtil.isEmpty(attendMeetingPeople)) {
            attendMeetingPeople = Arrays.asList(userId);
        } else {
            attendMeetingPeople.add(userId);
        }
        orderMeetingVO.setAttendMeetingPeople(attendMeetingPeople.stream().distinct().collect(Collectors.toList()));

        // 生成短租订单
        if (orderMeetingVO.getOrderType().equals(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING)) {
            orderMeetingList = delShortMeetingOrder(orderMeetingVO, sysUserDTO, userId, tenantCode, nowDate, orderNo, conf, orgId, orgName);
        }

        // 生成长租订单
        if (orderMeetingVO.getOrderType().equals(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING)) {
            orderMeetingList = dealLongMeetingOrder(orderMeetingVO, sysUserDTO, tenantCode, nowDate, orderNo, conf, orgId, orgName);
        }

        Integer isNeedApprove = null != conf.getIsNeedApprove() ? conf.getIsNeedApprove() : OrderConstant.NeedApprove.NOT_APPROVE.getCode();

        //不需要审批并且是短租时，生成会议室订单和参会人的数据 （需要审批不知道是审批通过还是审批不通过，审批不通过不需要添加这些数据,不需要审批，长租需要等开启会议室才生成这些数据）
        if (OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(isNeedApprove) && OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            //生成会议室记录和会议室订单与参会人记录
            batchInsertOrderMeetingInfo(orderMeetingList, attendMeetingPeople);
        }

        //需要审批则插入一条审批记录
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

        //不需要审批发送参会邀请通知
        if (OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(isNeedApprove)) {
            orderUtils.sendNoticeToAppointmentPeople(orderMeetingList.getAttendMeetingPeople(), orderMeetingList.getOrderMeetingInfoId(), orderMeetingList);
        }

        String userSubCountKey = tenantCode + ":order:userSubCount:" + userId;
        //缓存内用户当日预约次数加一
        redisRepository.increasing(userSubCountKey, 1);
        //检查用户是否存在，不存在就新增一条记录,存在则添加预约次数
        orderUtils.addOrUpdateOrderUser(sysUserDTO.getId(), orderMeetingList, orgId);
        return R.ok(orderMeetingList);
    }

    @Override
    @Transactional
    public R<String> approve(String orderMeetingId, Integer state) {
        log.info("OrderMeetingServiceImpl.approve msg: orderMeetingId:" + orderMeetingId + ",state:" + state);
        CommonUtil.notNullOrEmpty(orderMeetingId, "订单id不能为空!");
        CommonUtil.notNull(state, "审批状态不能为空!");
        CommonUtil.check(Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.APPROVE_FAILED.getAftId()).contains(state), "不存在此状态!");

        LambdaQueryWrapper<OrderMeetingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(wrapper);
        if (Objects.isNull(orderMeetingList)) {
            throw BusinessException.of("数据库未找到该订单");
        }

        LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(OrderMeetingList::getState, state)
                .set(OrderMeetingList::getUpdateTime, new Date())
                .eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        orderMeetingListMapper.update(null, updateWrapper);
        orderMeetingList.setState(state);

        List<String> attendPeopleIds = CommonUtil.stringToList(orderMeetingList.getAttendMeetingPeople());
        //审核通过，短租审核通过生成订单详情表，长租审核通过等开启会议在生成会议室预约记录
        if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state) && OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            batchInsertOrderMeetingInfo(orderMeetingList, attendPeopleIds);
        }

        //审核通过，长租审核通过生成会议订单与参会人详情表，长租审核通过等开启会议在生成会议室预约记录
        if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state) && OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            batchInsertOrderMeetingUser(orderMeetingList, null, attendPeopleIds);
        }

        return R.ok("修改状态成功");
    }

    @Override
    @Transactional
    public R<ListWithPage<UserMeetingListRepVO>> getMeetingOrderPage(UserMeetingOrderReqVO userMeetingOrderReqVO) {
        log.info("OrderMeetingServiceImpl.getMeetingOrderPage msg: " + JSONObject.toJSONString(userMeetingOrderReqVO));
        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        userMeetingOrderReqVO.setUserId(userId);

        // 计算查看订单截止日期
        Integer timeRange = userMeetingOrderReqVO.getTimeRange();
        if (null != timeRange && !timeRange.equals(0)) {
            Date date = DateUtil.offsetMonth(new Date(), -timeRange).toJdkDate();
            userMeetingOrderReqVO.setEndTime(date);
        }

        ListWithPage<UserMeetingListRepVO> listWithPage = new ListWithPage<>();

        //从签到记录表获取用户参与的会议订单id集合
        //从会议室订单和参会人表中获取参会人的会议室订单
        LambdaQueryWrapper<OrderMeetingUser> meetingUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingUserLambdaQueryWrapper.eq(OrderMeetingUser::getUserId, userId);

        List<OrderMeetingUser> meetingUserList = orderMeetingUserMapper.selectList(meetingUserLambdaQueryWrapper);
        List<String> meetingIds = meetingUserList.stream().map(OrderMeetingUser::getOrderMeetingId).distinct().collect(Collectors.toList());
        userMeetingOrderReqVO.setMeetingIds(meetingIds);
        List<Integer> states = Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId(), OrderConstant.MeetingStateEnum.FINISH.getAftId());
        //获取总数
        Integer count = orderMeetingListMapper.getCount(userMeetingOrderReqVO);
        if (count <= 0) {
            return R.ok(listWithPage);
        }

        //获取所有的打卡记录
        List<OrderMeetingRentDetail> rentDetailList = new ArrayList<>();
        if (!CollectionUtil.isEmpty(meetingIds)) {
            LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailQueryWrapper = new LambdaQueryWrapper<>();
            rentDetailQueryWrapper.in(OrderMeetingRentDetail::getOrderMeetingId, meetingIds);
            rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getUserId, userId);
            rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailQueryWrapper);
        }

        //获取分页数据
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
                //添加签到状态
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
        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        String orderMeetingId = orderMeetingInfoReq.getOrderMeetingId();
        CommonUtil.notNull(orderMeetingId, "订单id不能为空");

        //获取订单数据
        OrderMeetingListDetailRepVO orderMeeting = orderMeetingListMapper.getOrderDetail(orderMeetingId);

        Integer orderType = orderMeeting.getOrderType();

        Date orderStartTime = orderMeeting.getOrderStartTime();
        Date orderEndTime = orderMeeting.getOrderEndTime();
        // 短租拼接返回时间
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            String time = DateUtil.formatDate(orderStartTime) + " " + DateUtil.format(orderStartTime, "HH:mm") + " ~ " + DateUtil.format(orderEndTime, "HH:mm");
            orderMeeting.setTime(time);
        }

        // 长租拼接返回时间
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderType)) {
            String time = DateUtil.formatDate(orderStartTime) + " ~ " + DateUtil.formatDate(orderEndTime);
            orderMeeting.setTime(time);
        }

        //获取该用户此订单下所有的打卡记录
        LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailQueryWrapper = new LambdaQueryWrapper<>();
        rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingId);
        rentDetailQueryWrapper.eq(OrderMeetingRentDetail::getUserId, userId);
        List<OrderMeetingRentDetail> rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailQueryWrapper);

        /**添加参会人**/
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
        //获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        Long timestamp = System.currentTimeMillis();
        List<Integer> states = Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId());

        //从会议室订单和参会人关系表会议订单id集合
        LambdaQueryWrapper<OrderMeetingUser> meetingUserQueryWrapper = new LambdaQueryWrapper<>();
        meetingUserQueryWrapper.eq(OrderMeetingUser::getUserId, sysUserDTO.getId());
        List<OrderMeetingUser> orderMeetingUserList = orderMeetingUserMapper.selectList(meetingUserQueryWrapper);
        if (CollectionUtil.isEmpty(orderMeetingUserList)) {
            return R.ok(new ArrayList<>());
        }
        List<String> orderMeetingIds = orderMeetingUserList.stream().map(OrderMeetingUser::getOrderMeetingId).collect(Collectors.toList());
        //获取所有的会议室订单信息（当前在预约期间的），非待审核，审核不通过，已完成，取消
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
        //今天凌晨
        Date beginOfDay = DateUtil.beginOfDay(new Date()).toJdkDate();
        Date endOfDay = DateUtil.endOfDay(new Date()).toJdkDate();
        //先查今天的短租
        List<MeetingClockRepVO> orderMeetingListShort = orderMeetingListMapper.getShortClockInfoByDate(orderMeetingIds, beginOfDay, endOfDay, states, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);

        //今天没有，就查询最近的日期的短租
        if (CollectionUtil.isEmpty(orderMeetingListShort)) {
            Date shortLatestDate = orderMeetingListMapper.getShortLatestDate(orderMeetingIds, states, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);
            if (null != shortLatestDate) {
                Date beginShortLatestOfDay = DateUtil.beginOfDay(shortLatestDate).toJdkDate();
                Date endShortLatestOfDay = DateUtil.endOfDay(shortLatestDate).toJdkDate();
                orderMeetingListShort = orderMeetingListMapper.getShortClockInfoByDate(orderMeetingIds, beginShortLatestOfDay, endShortLatestOfDay, states, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);
            }
        }
        //查询所有的长租
        List<MeetingClockRepVO> orderMeetingListLong = orderMeetingListMapper.getLongClockInfoByDate(orderMeetingIds, Arrays.asList(OrderConstant.MeetingStateEnum.IN_USE.getAftId()), OrderConstant.OrderType.LONG_RENT_ORDER_MEETING);

        clockInfo.addAll(orderMeetingListLong);
        clockInfo.addAll(orderMeetingListShort);
        //获取到所有的
        if (CollectionUtil.isEmpty(clockInfo)) {
            return R.ok(clockInfo);
        }

        //获取所有的打卡记录
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
                //获取规则
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

                /** 短租 **/
                if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(clockRepVO.getOrderType())) {
                    //设置短租使用时长
                    clockRepVO.setOrderTimeRange(OrderUtils.twoTimeDiffer(orderStartTime, orderEndTime));

                    //添加签到状态和可签到时间等
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
                        log.error("订单已结束，不应该展示在此处");
                    }
                }

                /** 长租 **/
                if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(clockRepVO.getOrderType())) {
                    //设置长租使用时长
                    Long useTime = null != clockRepVO.getUseTime() ? clockRepVO.getUseTime() : 0L;
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    double a = useTime / (1000 * 3600.0);
                    clockRepVO.setLongUseTime(decimalFormat.format(a));
                    //添加签到状态
                    if (StringUtils.isBlank(clockRepVO.getOrderMeetingInfoId())) {
                        clockRepVO.setSignState(OrderConstant.MeetingSignStateEnum.NOT_SIGN_TIME.getCode());
                    } else {
                        //获取最新的会议室记录
                        long count = rentDetailList.stream().filter(item -> item.getOrderMeetingInfoId().equals(clockRepVO.getOrderMeetingInfoId())).count();

                        if (OrderConstant.MeetingStateEnum.IN_USE.getAftId().equals(state)) {
                            clockRepVO.setSignState(count <= 0 ? OrderConstant.MeetingSignStateEnum.TO_BE_SIGN.getCode() : OrderConstant.MeetingSignStateEnum.SIGNED.getCode());
                        } else {
                            log.error("非进行中状态的订单不应该展示在此处");
                        }
                    }
                }
            } catch (ParseException e) {
                throw BusinessException.of("转换时间格式出错");
            }
        }

        return R.ok(clockInfo);
    }

    @Override
    @Transactional
    public R<String> cancelOrder(CancelMeetingOrderReqVO cancelMeetingOrderReqVO) {
        log.info("OrderMeetingServiceImpl.cancelOrder msg: " + JSONObject.toJSONString(cancelMeetingOrderReqVO));
        // 获取传参
        String id = cancelMeetingOrderReqVO.getId();

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        return orderMeetingLockService.lockCancelOrder(id, tenantCode, userId);
    }

    @Override
    @Transactional
    public R<String> arriveOrder(ArriveMeetingOrderReqVO arriveMeetingOrderReqVO) {
        log.info("OrderMeetingServiceImpl.arriveOrder msg: " + JSONObject.toJSONString(arriveMeetingOrderReqVO));
        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String tenantCode = sysUserDTO.getTenantCode();
        String userId = sysUserDTO.getId();

        // 获取传参信息
        Integer clockType = arriveMeetingOrderReqVO.getClockType();
        String orderMeetingId = arriveMeetingOrderReqVO.getOrderMeetingId();
        Integer distance = arriveMeetingOrderReqVO.getDistance();
        Date nowDate = new Date();


        //获取父订单 orderMeeting
        LambdaQueryWrapper<OrderMeetingList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(queryWrapper);
        CommonUtil.notNull(orderMeetingList, "不存在此会议室订单");
        CommonUtil.check(StringUtils.isNotEmpty(orderMeetingList.getOrderMeetingInfoId()), "不存在会议室记录，暂时无法打卡");
        CommonUtil.check(Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderMeetingList.getOrderType()), "不支持的订单类型");
        //短租签到人数到一定数量会自动改为使用中，长租需要会议室为使用中才可以
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
            CommonUtil.check(Objects.equals(OrderConstant.MeetingStateEnum.IN_USE.getAftId(), orderMeetingList.getState()), "会议还未开始，暂不支持打卡");
        }

        //获取会议室记录
        OrderMeetingInfo latestOrderMeetingInfo = orderMeetingInfoMapper.getOrderMeetingInfoByInfoId(orderMeetingList.getOrderMeetingInfoId());
        CommonUtil.notNull(latestOrderMeetingInfo, "会议室记录不存在，暂时无法打卡");
        String orderMeetingInfoId = latestOrderMeetingInfo.getOrderMeetingInfoId();

        SpaceConfVO conf = orderUtils.getRoomConf(orderMeetingList.getRoomId());
        /*List<SpaceConfSignVO> signList = conf.getSignList();
        List<Integer> codeList;
        if (CollectionUtil.isNotEmpty(signList)) {
            codeList = signList.stream().map(SpaceConfSignVO::getSignId).map(Integer::parseInt).collect(Collectors.toList());
        } else {
            codeList = Arrays.asList(OrderConstant.clockType.DISTANCE);
        }

        //检查打卡类型
        if (CollectionUtil.isEmpty(codeList)) {
            codeList = Arrays.asList(OrderConstant.clockType.DISTANCE);
        }
        CommonUtil.check(CollectionUtil.isNotEmpty(codeList), "暂不支持打卡，请联系管理员");
        StringBuilder clockWayBuilder = new StringBuilder();
        for (Integer s : codeList) {
            if (OrderConstant.clockType.DISTANCE.equals(s)) {
                clockWayBuilder.append("位置打卡，");
            }
            if (OrderConstant.clockType.CODE.equals(s)) {
                clockWayBuilder.append("扫码打卡，");
            }
        }
        String clockWayStr = clockWayBuilder.toString();
        if (clockWayStr.endsWith("，")) {
            clockWayStr = clockWayStr.substring(0, clockWayStr.length() - 1);
        }
        CommonUtil.check(StringUtils.isNotBlank(clockWayStr), "订单异常");
        CommonUtil.check(codeList.contains(clockType), "该订单支持 " + clockWayStr);*/

        // 获取打卡最短距离
        int signMaxDistance = null != conf.getSignMaxDistance() ? conf.getSignMaxDistance() : 100;

        // 距离方式打卡签到
        if (OrderConstant.clockType.DISTANCE.equals(clockType)) {
            CommonUtil.check(signMaxDistance >= distance, "距离过远，无法打卡");
        }

        //防止客户重复签到
        LambdaQueryWrapper<OrderMeetingRentDetail> queryWrapperMeeting = new LambdaQueryWrapper<>();
        queryWrapperMeeting.eq(OrderMeetingRentDetail::getOrderMeetingInfoId, orderMeetingList.getOrderMeetingInfoId());
        queryWrapperMeeting.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
        queryWrapperMeeting.eq(OrderMeetingRentDetail::getUserId, userId);
        //获取到当前用户签到记录
        OrderMeetingRentDetail orderMeetingRentDetail = orderMeetingRentDetailMapper.selectOne(queryWrapperMeeting);
        CommonUtil.isNull(orderMeetingRentDetail, "请勿重复打卡");

        // 生成分布式锁key
        String lockKey = tenantCode + ":meeting_order:arrive-key-lock:" + "-" + userId + "-" + orderMeetingInfoId;
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        try {
            //短租迟到超过多久不可打卡
            if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType())) {
                //去除迟到多久不可签到，整场可以签到
//                Integer signLateTime = null != conf.getSignLateTime() ? conf.getSignLateTime() : 30;
                Integer signAdvanceTime = null != conf.getSignAdvanceTime() ? conf.getSignAdvanceTime() : 30;

//                CommonUtil.check(System.currentTimeMillis() <= (latestOrderMeetingInfo.getUserStartTime().getTime() + signLateTime * 60 * 1000), "迟到超过" + signLateTime + "分钟不可打卡");
                CommonUtil.check(System.currentTimeMillis() >= (latestOrderMeetingInfo.getUserStartTime().getTime() - signAdvanceTime * 60 * 1000), "提前" + signAdvanceTime + "分钟可以签到");

                //会议状态还是待开始
                if (OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(orderMeetingList.getState())) {
                    //如果满足打卡人数，将orderMeeting状态改为进行中
                    LambdaQueryWrapper<OrderMeetingRentDetail> queryWrapperMeetingInner = new LambdaQueryWrapper<>();
                    queryWrapperMeetingInner.eq(OrderMeetingRentDetail::getOrderMeetingInfoId, orderMeetingInfoId);
                    queryWrapperMeetingInner.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
                    Integer arriveCount = orderMeetingRentDetailMapper.selectCount(queryWrapperMeetingInner);
                    //开启预约的人数
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
            //插入用户签到信息
            OrderMeetingRentDetail detail = OrderMeetingUtil.getOrderMeetingRentDetail(orderMeetingList.getOrderMeetingId(), orderMeetingInfoId, sysUserDTO);
            orderMeetingRentDetailMapper.insert(detail);
        } finally {
            lock.unlock();
        }
        return R.ok("打卡成功");
    }

    @Override
    @Transactional
    public R<ListWithPage<OrderMeetingRentDetailRepVO>> showMeetingRentDetail(MeetingClockDetailReqVO clockDetailReqVO) {
        log.info("OrderMeetingServiceImpl.showMeetingRentDetail msg: " + JSONObject.toJSONString(clockDetailReqVO));
        //获取操作用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();

        CommonUtil.notNull(clockDetailReqVO.getOrderMeetingInfoId(), "订单id不能为空");
        LambdaQueryWrapper<OrderMeetingInfo> meetingInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingInfoLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingInfoId, clockDetailReqVO.getOrderMeetingInfoId());
        OrderMeetingInfo orderMeetingInfo = orderMeetingInfoMapper.selectOne(meetingInfoLambdaQueryWrapper);
        CommonUtil.notNull(orderMeetingInfo, "不存在此会议室记录");
        LambdaQueryWrapper<OrderMeetingList> meetingListLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingListLambdaQueryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingInfo.getOrderMeetingId());
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(meetingListLambdaQueryWrapper);
        CommonUtil.notNull(orderMeetingInfo, "会议室记录信息异常");

        LambdaQueryWrapper<OrderMeetingRentDetail> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        detailLambdaQueryWrapper.select(OrderMeetingRentDetail::getUserId, OrderMeetingRentDetail::getUserName, OrderMeetingRentDetail::getUseStartTime, OrderMeetingRentDetail::getUseEndTime);
        //非预约人只能查看自己的签到记录
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
        //获取操作用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        Date nowDate = new Date();
        String orderMeetingId = openCloseMeetingReqVO.getOrderMeetingId();
        CommonUtil.notNull(orderMeetingId, "订单id不能为空!");
        CommonUtil.notNull(openCloseMeetingReqVO.getState(), "状态不能为空!");
        CommonUtil.check(Arrays.asList(OrderConstant.OpenCloseMeeting.OPEN_APPOINTMENT, OrderConstant.OpenCloseMeeting.CLOSE_APPOINTMENT).contains(openCloseMeetingReqVO.getState()), "不支持的状态");
        //获取订单
        LambdaQueryWrapper<OrderMeetingList> queryWrapper = new LambdaQueryWrapper<>();
        // 查询订单状态和订单开始时间，若订单状态不为待签到，则不可签到，并判断是否迟到
        queryWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId).eq(OrderMeetingList::getUserId, userId);
        OrderMeetingList orderMeetingList = orderMeetingListMapper.selectOne(queryWrapper);
        CommonUtil.notNull(orderMeetingList, "不存在此订单");
        //检查是否到了开启时间
        CommonUtil.check(System.currentTimeMillis() >= orderMeetingList.getOrderStartTime().getTime(), "未到预约时间，暂不支持操作会议室");
        Integer state = orderMeetingList.getState();
        CommonUtil.notNull(orderMeetingList, "订单不存在");
        CommonUtil.check(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(orderMeetingList.getOrderType()), "非长租订单不可进行此操作");
        CommonUtil.check(Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId()).contains(state), "该订单已不可操作!");

        LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderMeetingList::getOrderMeetingId, orderMeetingId);
        //只有待开始才可以开启预约
        //开启预约 将其状态改为进行中
        if (OrderConstant.OpenCloseMeeting.OPEN_APPOINTMENT.equals(openCloseMeetingReqVO.getState())) {
            CommonUtil.check(OrderConstant.MeetingStateEnum.TO_START.getAftId().equals(state), "非待开始状态不可开启会议");
            updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.IN_USE.getAftId())
                    .set(OrderMeetingList::getUpdateTime, nowDate)
                    .eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
            orderMeetingListMapper.update(null, updateWrapper);
            orderMeetingList.setState(OrderConstant.MeetingStateEnum.IN_USE.getAftId());

            List<String> userIds = orderUtils.getUserIds(orderMeetingList.getAttendMeetingPeople(), orderMeetingList.getOrderMeetingInfoId());

            batchInsertOrderMeetingInfo(orderMeetingList, userIds);
            return R.ok("开启会议成功");
        }
        //只有进行中才可以关闭预约
        //关闭预约 将其状态改为待开始
        if (OrderConstant.OpenCloseMeeting.CLOSE_APPOINTMENT.equals(openCloseMeetingReqVO.getState())) {
            //获取最新的会议室记录
            LambdaQueryWrapper<OrderMeetingInfo> meetingInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            meetingInfoLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingId, orderMeetingId);
            meetingInfoLambdaQueryWrapper.orderByDesc(OrderMeetingInfo::getCreateTime);
            meetingInfoLambdaQueryWrapper.last("limit 1");
            OrderMeetingInfo orderMeetingInfo = orderMeetingInfoMapper.selectOne(meetingInfoLambdaQueryWrapper);
            CommonUtil.notNull(orderMeetingInfo, "不存在会议室记录，不可关闭");

            CommonUtil.check(OrderConstant.MeetingStateEnum.IN_USE.getAftId().equals(state), "非进行中状态不可关闭会议");
            Long longUseTime = null != orderMeetingList.getUseTime() ? orderMeetingList.getUseTime() : 0;
            long time = nowDate.getTime() - orderMeetingInfo.getUserStartTime().getTime();
            Long useTime = longUseTime + time;
            updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.TO_START.getAftId())
                    .set(OrderMeetingList::getUpdateTime, nowDate)
                    .set(OrderMeetingList::getUseTime, useTime)
                    .set(OrderMeetingList::getOrderMeetingInfoId, null)
                    .eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
            orderMeetingListMapper.update(null, updateWrapper);
            //将会议室预约记录的结束时间改成当前时间
            LambdaUpdateWrapper<OrderMeetingInfo> updateWrapperMeetingInfo = new LambdaUpdateWrapper<>();
            updateWrapperMeetingInfo.set(OrderMeetingInfo::getState, OrderConstant.MeetingStateEnum.FINISH.getAftId())
                    .set(OrderMeetingInfo::getMeetingState, OrderConstant.MeetingStateEnum.FINISH.getAftId())
                    .set(OrderMeetingInfo::getUpdateTime, nowDate)
                    .set(OrderMeetingInfo::getUseEndTime, nowDate)
                    .eq(OrderMeetingInfo::getOrderMeetingInfoId, orderMeetingInfo.getOrderMeetingInfoId())
                    .eq(OrderMeetingInfo::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
            orderMeetingInfoMapper.update(null, updateWrapperMeetingInfo);

            //刷新用户的使用时间
            orderUtils.updateOrderUserLearnTotalTime(orderMeetingList.getUserId(), time);
            return R.ok("关闭会议成功");
        }
        return R.ok("操作失败");
    }

    @Override
    @Transactional
    public R<ListWithPage<UserMeetingInfoRepVO>> getMeetingRecord(UserMeetingListReqVO userMeetingListReqVO) {
        log.info("OrderMeetingServiceImpl.getMeetingRecord msg: " + JSONObject.toJSONString(userMeetingListReqVO));
        String orderMeetingId = userMeetingListReqVO.getOrderMeetingId();
        CommonUtil.notNull(orderMeetingId, "订单id不能为空");
        //获取订单
        OrderMeetingListDetailRepVO orderMeeting = orderMeetingListMapper.getOrderDetail(orderMeetingId);
        CommonUtil.notNull(orderMeeting, "该会议室订单不存在");

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
        //获取到预约记录的id集合
        List<String> orderMeetingInfoIds = records.stream().map(OrderMeetingInfo::getOrderMeetingInfoId).collect(Collectors.toList());
        //根据预约记录id集合获取签到记录
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
            return R.error("id集合为空");
        }
        if (!Arrays.asList(SpaceConstant.SpaceType.BUILD, SpaceConstant.SpaceType.FLOOR, SpaceConstant.SpaceType.ROOM).contains(spaceType)) {
            return R.error("不支持的空间类型");
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
        //待开始，进行中，审批中状态的将其改为系统取消，其余 已完成，审核通过，系统取消不修改
        updateWrapper.in(OrderMeetingList::getState, Arrays.asList(OrderConstant.MeetingStateEnum.TO_START.getAftId(), OrderConstant.MeetingStateEnum.IN_USE.getAftId(), OrderConstant.MeetingStateEnum.TO_BE_APPROVE.getAftId()));
        updateWrapper.set(OrderMeetingList::getState, OrderConstant.MeetingStateEnum.SYSTEM_CANCEL.getAftId());
        orderMeetingListMapper.update(null, updateWrapper);
        return R.ok("处理成功");
    }

    @Override
    public R<List<SysUserOrgTreeDTO>> getTreeUsers() {
        log.info("OrderMeetingServiceImpl.getMeetingRecord ");
        List<SysUserOrgTreeDTO> list = new ArrayList<>();
        //1.查询所有的组织
        List<SysOrg> sysOrgList = sysOrgService.getOrgList(new SysOrg());
        OrgTree orgTree = new OrgTree(sysOrgList);
        orgTree.mergeChild(sysOrgList);
        //2.用户表左联用户组织表获取所有的数据
        List<SysUser> userList = sysUserMapper.getUserAndOrgData();
        //3.将用户组织数据数据转换成map key:组织，value:用户数据
        //没有组织的用户
        List<SysUser> hasOrgUsers = userList.stream().filter(item -> ObjectUtil.isNotNull(item.getOrgId())).collect(Collectors.toList());
        Map<String, List<SysUser>> orgUserMap = hasOrgUsers.stream().collect(Collectors.groupingBy(SysUser::getOrgId));
        //4.组装成树形结构
        //第一级组织
        List<SysOrg> firstLevel = sysOrgList.stream().filter(item -> null == item.getPid() || "-1".equals(item.getPid())).collect(Collectors.toList());
        for (SysOrg sysOrg : firstLevel) {
            list.addAll(getTreeOrg(sysOrg, sysOrgList, orgUserMap));
        }
        //5.没有组织的用户放第一列
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

        if (StringUtils.isBlank(id)) { //查询组织，pid为空的返回
            //第一级组织(无父id的组织)
            List<SysOrg> sysNoPidOrgList = sysOrgMapper.getNoPidOrg();
            OrgTree orgTree = new OrgTree(sysNoPidOrgList);
            orgTree.mergeChild(sysNoPidOrgList);
            //第一级用户(无组织的用户)
            List<SysUser> noOrgUserList = sysUserMapper.getNoOrgUser();
            list.addAll(sysNoPidOrgList.stream().map(this::getSysUserTreeDTOBySysOrg).collect(Collectors.toList()));
            list.addAll(noOrgUserList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList()));
            return R.ok(list);
        }
        if (ObjectUtil.isNull(user) || !user) { //组织
            if (StringUtils.isNotBlank(id)) {
                //子组织
                SysOrg sysOrg = new SysOrg();
                HashSet<String> set = new HashSet<>();
                set.add(id);
                sysOrg.setOrgIds(set);
                List<SysOrg> orgPage = sysOrgMapper.getSonOrg(sysOrg);
                list.addAll(orgPage.stream().map(this::getSysUserTreeDTOBySysOrg).collect(Collectors.toList()));
                //子用户
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
        //获取会议室主体信息
        OrderMeetingDetailDTO meetingDetailDTO = orderMeetingListMapper.getMeetingDetail(orderMeetingId, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING);
        CommonUtil.notNull(meetingDetailDTO, "不存在此预约记录!");
        //获取签到信息
        LambdaQueryWrapper<OrderMeetingRentDetail> rentDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        rentDetailLambdaQueryWrapper.eq(OrderMeetingRentDetail::getOrderMeetingId, orderMeetingId);
        List<OrderMeetingRentDetail> rentDetailList = orderMeetingRentDetailMapper.selectList(rentDetailLambdaQueryWrapper);
        OrderMeetingShortDetailRepVO detailRepVO = OrderUtils.getShortDetailRepVoByMeetingDetailDTO(meetingDetailDTO, rentDetailList);
        return R.ok(detailRepVO);
    }

    @Override
    public R<OrderMeetingLongDetailRepVO> getLongDetail(String orderMeetingId) {
        log.info("OrderMeetingServiceImpl.getLongDetail msg: " + orderMeetingId);
        //获取会议室主体信息
        OrderMeetingDetailDTO meetingDetailDTO = orderMeetingListMapper.getMeetingDetail(orderMeetingId, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING);
        CommonUtil.notNull(meetingDetailDTO, "不存在此预约记录!");
        //获取会议室记录信息
        LambdaQueryWrapper<OrderMeetingInfo> meetingInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingInfoLambdaQueryWrapper.eq(OrderMeetingInfo::getOrderMeetingId, meetingDetailDTO.getOrderMeetingId()).orderByDesc(OrderMeetingInfo::getUserStartTime);
        List<OrderMeetingInfo> orderMeetingInfoList = orderMeetingInfoMapper.selectList(meetingInfoLambdaQueryWrapper);
        //获取所有签到记录
        //获取签到信息
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
            //添加组织下的用户
            List<SysUser> sysUserList = orgUserMap.get(sysOrg.getId());
            if (CollectionUtil.isNotEmpty(sysUserList)) {
                List<SysUserOrgTreeDTO> noOrgUserOrgDTO = sysUserList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList());
                children.addAll(noOrgUserOrgDTO);
            }
            //组织下的组织进行递归
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
            //添加组织下的用户
            List<SysUser> sysUserList = orgUserMap.get(sysOrg.getId());
            if (CollectionUtil.isNotEmpty(sysUserList)) {
                List<SysUserOrgTreeDTO> noOrgUserOrgDTO = sysUserList.stream().map(this::getSysUserTreeDTOBySysUser).collect(Collectors.toList());
                children.addAll(noOrgUserOrgDTO);
            }
            //组织下的组织进行递归
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
        //生成会议室记录
        List<OrderMeetingInfo> detailList = new ArrayList<>();

        OrderMeetingInfo orderMeetingInfo = OrderMeetingUtil.getOrderMeetingInfo(orderMeetingList);
        detailList.add(orderMeetingInfo);
        orderMeetingInfoMapper.batchInsert(detailList);
        LambdaUpdateWrapper<OrderMeetingList> orderMeetingListLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        orderMeetingListLambdaUpdateWrapper.set(OrderMeetingList::getOrderMeetingInfoId, orderMeetingInfo.getOrderMeetingInfoId()).eq(OrderMeetingList::getOrderMeetingId, orderMeetingList.getOrderMeetingId());
        orderMeetingListMapper.update(null, orderMeetingListLambdaUpdateWrapper);

        //插入会议室与参会人数据
        batchInsertOrderMeetingUser(orderMeetingList, orderMeetingInfo.getOrderMeetingInfoId(), orderMeetingPeopleIds);
    }

    /**
     * 批量添加会议室参会人数据
     *
     * @param orderMeetingList
     * @return
     */
    private void batchInsertOrderMeetingUser(OrderMeetingList orderMeetingList, String orderMeetingInfoId, List<String> orderMeetingPeopleIds) {
        //生成会议室与参会人数据
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
     * 功能描述: <br>
     * 〈会议室预约长租订单处理〉
     *
     * @return
     * @Param: [orderMeetingVO, sysUserDTO, tenantCode, orderNo]
     * @Return: void
     * @Author: 王鹏滔
     * @Date: 2021/6/9 11:04
     */
    private OrderMeetingList dealLongMeetingOrder(OrderMeetingVO orderMeetingVO, SysUserDTO sysUserDTO, String tenantCode, Date nowDate, String orderNo, SpaceConfVO conf, String orgId, String orgName) {
        //从VO获取entity
        OrderMeetingList orderMeetingList = OrderMeetingUtil.getOrderMeeting(orderMeetingVO, sysUserDTO, orderNo, OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(null != conf.getIsNeedApprove() ? conf.getIsNeedApprove() : OrderConstant.NeedApprove.NOT_APPROVE.getCode()), orgId, orgName);
        orderMeetingList.setUserOrgId(orgId);
        orderMeetingList.setUserOrgName(orgName);
        //生成订单
        orderMeetingLockService.lockCreatOrder(orderMeetingList, tenantCode);
        // 消息队列：一个月后判断该订单是否被提前取消，若没有则将订单状态改为已完成
        OrderMQDTO orderMQDTO = new OrderMQDTO();
        // 将订单id封装，传入消息队列
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("orderMeetingId", orderMeetingList.getOrderMeetingId());
        orderMap.put("tenantCode", tenantCode);
        orderMap.put("sysUserDTO", sysUserDTO);

        String time = StrUtil.toString(DateUtil.betweenMs(nowDate, orderMeetingVO.getOrderEndTime()));
        //到期完成订单
        orderMQDTO.setRoutingKey(OrderConstant.routeKey.FINISH_MEETING_ROUTINGKEY).setTime(time).setParams(orderMap);
        rabbitMqUtils.sendLongMsg(orderMQDTO);
        return orderMeetingList;
    }

    /**
     * 功能描述: <br>
     * 〈会议室预约短租订单处理〉
     *
     * @return
     * @Param: [orderMeetingVO, sysUserDTO, tenantCode, orderNo]
     * @Return: void
     * @Author: 王鹏滔
     * @Date: 2021/6/9 11:04
     */
    private OrderMeetingList delShortMeetingOrder(OrderMeetingVO orderMeetingVO, SysUserDTO sysUserDTO, String userId, String tenantCode, Date nowDate, String orderNo, SpaceConfVO conf, String orgId, String orgName) {
        //从VO获取entity
        OrderMeetingList orderMeetingList = OrderMeetingUtil.getOrderMeeting(orderMeetingVO, sysUserDTO, orderNo, OrderConstant.NeedApprove.NOT_APPROVE.getCode().equals(null != conf.getIsNeedApprove() ? conf.getIsNeedApprove() : OrderConstant.NeedApprove.NOT_APPROVE.getCode()), orgId, orgName);
        //生成订单
        orderMeetingLockService.lockCreatOrder(orderMeetingList, tenantCode);

        OrderMQDTO orderMQDTO = new OrderMQDTO();
        //获取多久未开始释放时间，将其状态改为取消
        int meetingCancelTime = null != conf.getMeetingCancelCount() ? conf.getMeetingCancelCount() : 30;
        Integer signAdvanceTime = null != conf.getSignAdvanceTime() ? conf.getSignAdvanceTime() : 30;
        //计算延时时间
        long lateL = DateUtil.betweenMs(nowDate, orderMeetingList.getOrderStartTime()) / 1000L;
        int delayLateTime = meetingCancelTime * 60 + (int) lateL;
        HashMap<String, Object> orderMap = Maps.newHashMap();
        orderMap.put("orderMeetingId", orderMeetingList.getOrderMeetingId());
        orderMap.put("tenantCode", tenantCode);

        //消息队列，规定时间内打卡人数不足，将其改为取消
        OrderMQDTO orderLateMQDTO = orderMQDTO.setTime(Convert.toStr(delayLateTime * 1000)).setRoutingKey(OrderConstant.routeKey.CANCEL_MEETING_ROUTINGKEY).setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderLateMQDTO);

        //获取现在到预约结束时间间隔
        int waitLeaveTime = Convert.toInt(DateUtil.betweenMs(nowDate, orderMeetingVO.getOrderEndTime()));
        String strTime = Convert.toStr(waitLeaveTime);
        //将订单状态改为完成
        OrderMQDTO orderFinishMQDTO = orderMQDTO.setTime(strTime).setRoutingKey(OrderConstant.routeKey.FINISH_MEETING_ROUTINGKEY).setParams(orderMap);
        rabbitMqUtils.sendOrderMsg(orderFinishMQDTO);

        //可签到时间点通知
        Long nowToStart = DateUtil.betweenMs(nowDate, orderMeetingList.getOrderStartTime()) / 1000L;
        Long noticeTime = nowToStart - signAdvanceTime * 60;
        //微信打卡通知
        if (noticeTime > 0) {
            OrderMQDTO noticeSignDTO = orderMQDTO.setTime(Convert.toStr(noticeTime * 1000)).setRoutingKey(OrderConstant.routeKey.CLOCK_REMINDER_MEETING_ROUTINGKEY).setParams(orderMap);
            rabbitMqUtils.sendOrderMsg(noticeSignDTO);
        }

        return orderMeetingList;
    }

    /**
     * 获取最新的会议室记录
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
