package com.eseasky.modules.order.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.CommonUtil;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.StringUtils;
import com.eseasky.common.code.wx.WxProperties;
import com.eseasky.common.entity.SysUser;
import com.eseasky.common.service.SysUserService;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.InsertApproveInfoDTO;
import com.eseasky.modules.order.dto.OrderMeetingDetailDTO;
import com.eseasky.modules.order.dto.OrderNoticeDTO;
import com.eseasky.modules.order.entity.*;
import com.eseasky.modules.order.mapper.OrderMeetingInfoMapper;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.mapper.OrderMeetingUserMapper;
import com.eseasky.modules.order.mapper.OrderUserMapper;
import com.eseasky.modules.order.service.OrderNoticeService;
import com.eseasky.modules.order.vo.OrderMeetingVO;
import com.eseasky.modules.order.vo.response.*;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.request.QueryOrderRoomParam;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @describe:
 * @title: OrderUtil
 * @Author lc
 * @Date: 2021/4/20
 */
@Component
public class OrderUtils {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private OrderUserMapper orderUserMapper;

    @Autowired
    private OrderMeetingInfoMapper orderMeetingInfoMapper;

    @Autowired
    private OrderMeetingUserMapper orderMeetingUserMapper;

    @Autowired
    private OrderMeetingListMapper orderMeetingListMapper;

    @Autowired
    private SpaceRoomService spaceRoomService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OrderNoticeService orderNoticeService;

    @Autowired
    private WxProperties wxProperties;

    /**
     * ?????????????????????vo
     *
     * @param orderMeetingInfo
     * @param orderMeetingListDetailRepVO
     * @return
     */
    public static UserMeetingInfoRepVO getUserMeetingRecordVO(OrderMeetingInfo orderMeetingInfo, OrderMeetingListDetailRepVO orderMeetingListDetailRepVO) {
        UserMeetingInfoRepVO userMeetingInfoRepVO = new UserMeetingInfoRepVO();
        userMeetingInfoRepVO.setOrderMeetingInfoId(orderMeetingInfo.getOrderMeetingInfoId());
        userMeetingInfoRepVO.setOrderMeetingId(orderMeetingListDetailRepVO.getOrderMeetingId());
        userMeetingInfoRepVO.setOrderType(orderMeetingListDetailRepVO.getOrderType());
        userMeetingInfoRepVO.setState(orderMeetingListDetailRepVO.getState());
//        userMeetingInfoRepVO.setMeetingState(orderMeetingListDetailRepVO.getState());
        userMeetingInfoRepVO.setIsComment(orderMeetingInfo.getIsComment());
        userMeetingInfoRepVO.setOrderTime("0");
        if (null != orderMeetingInfo.getUserStartTime() && null != orderMeetingInfo.getUseEndTime()) {
            try {
                userMeetingInfoRepVO.setOrderTime(OrderUtils.twoTimeDiffer(DateUtil.formatDateTime(orderMeetingInfo.getUserStartTime()), DateUtil.formatDateTime(orderMeetingInfo.getUseEndTime())));
            } catch (ParseException e) {
                throw BusinessException.of("????????????????????????");
            }
        }
        userMeetingInfoRepVO.setArriveCount(0L);
        userMeetingInfoRepVO.setOrderStartTime(orderMeetingInfo.getUserStartTime());
        userMeetingInfoRepVO.setOrderEndTime(orderMeetingInfo.getUseEndTime());
        userMeetingInfoRepVO.setStartTime(DateUtil.formatDateTime(orderMeetingInfo.getUserStartTime()));
        userMeetingInfoRepVO.setEndTime(DateUtil.formatDateTime(orderMeetingInfo.getUseEndTime()));
        userMeetingInfoRepVO.setBuildName(orderMeetingListDetailRepVO.getBuildName());
        userMeetingInfoRepVO.setFloorNum(orderMeetingListDetailRepVO.getFloorNum());
        userMeetingInfoRepVO.setRoomName(orderMeetingListDetailRepVO.getRoomName());
        userMeetingInfoRepVO.setRoomNum(orderMeetingListDetailRepVO.getRoomNum());
        userMeetingInfoRepVO.setRoomId(orderMeetingListDetailRepVO.getRoomNum());
        return userMeetingInfoRepVO;
    }

    /**
     * ???????????????entity ?????? vo
     *
     * @param orderMeetingRentDetail
     * @return
     */
    public static OrderMeetingRentDetailRepVO getOrderMeetingRentDetailRepVO(OrderMeetingRentDetail orderMeetingRentDetail) {
        OrderMeetingRentDetailRepVO orderMeetingRentDetailRepVO = new OrderMeetingRentDetailRepVO();
        orderMeetingRentDetailRepVO.setOrderMeetingInfoId(orderMeetingRentDetail.getOrderMeetingInfoId());
        orderMeetingRentDetailRepVO.setOrderMeetingId(orderMeetingRentDetail.getOrderMeetingId());
        orderMeetingRentDetailRepVO.setUseStartTime(orderMeetingRentDetail.getUseStartTime());
        orderMeetingRentDetailRepVO.setUseEndTime(orderMeetingRentDetail.getUseEndTime());
        orderMeetingRentDetailRepVO.setListUseTime(orderMeetingRentDetail.getListUseTime());
        orderMeetingRentDetailRepVO.setUseDay(orderMeetingRentDetail.getUseDay());
        orderMeetingRentDetailRepVO.setIsLeave(orderMeetingRentDetail.getIsLeave());
        orderMeetingRentDetailRepVO.setUserId(orderMeetingRentDetail.getUserId());
        orderMeetingRentDetailRepVO.setUserName(orderMeetingRentDetail.getUserName());
        orderMeetingRentDetailRepVO.setCreateTime(orderMeetingRentDetail.getCreateTime());
        orderMeetingRentDetailRepVO.setUpdateTime(orderMeetingRentDetail.getUpdateTime());
        orderMeetingRentDetailRepVO.setDelFlag(orderMeetingRentDetail.getDelFlag());
        return orderMeetingRentDetailRepVO;
    }

    public static InsertApproveInfoDTO getInsertApproveInfoDTO(OrderMeetingList orderMeetingList, OneBOneFOneR roomInfo, String[] approvers) {
        InsertApproveInfoDTO insertApproveInfoDTO = new InsertApproveInfoDTO();
        insertApproveInfoDTO.setOrderListId(orderMeetingList.getOrderMeetingId());
        insertApproveInfoDTO.setOrderType(orderMeetingList.getOrderType());
        insertApproveInfoDTO.setUserId(orderMeetingList.getUserId());
        insertApproveInfoDTO.setUserName(orderMeetingList.getUserName());
        insertApproveInfoDTO.setApplyTime(new Date());
        insertApproveInfoDTO.setReason("");
        insertApproveInfoDTO.setOrderStartTime(orderMeetingList.getOrderStartTime());
        insertApproveInfoDTO.setOrderEndTime(orderMeetingList.getOrderEndTime());
        insertApproveInfoDTO.setArea(roomInfo.getBuildName() + "-" + roomInfo.getFloorName() + "-" + roomInfo.getRoomName());
        insertApproveInfoDTO.setApprovers(approvers);

        return insertApproveInfoDTO;

    }

    /**
     * ??????orderNotice DTO
     *
     * @param meetingDetail
     * @return
     */
    public static OrderNoticeDTO getOrderNoticeDTOByMeetingList(OrderMeetingListDetailRepVO meetingDetail, String noticeContent, String approveState) {
        OrderNoticeDTO orderNoticeDTO = new OrderNoticeDTO();
        orderNoticeDTO.setOrderListId(meetingDetail.getOrderMeetingId());
        orderNoticeDTO.setOrderMeetingInfoId(meetingDetail.getOrderMeetingInfoId());
        orderNoticeDTO.setUserName(meetingDetail.getUserName());
        orderNoticeDTO.setOrderType(meetingDetail.getOrderType());
        orderNoticeDTO.setTheme(meetingDetail.getTheme());
        //????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(meetingDetail.getOrderType())) {
            orderNoticeDTO.setOrderStartTime(DateUtil.formatDateTime(meetingDetail.getOrderStartTime()));
            orderNoticeDTO.setOrderEndTime(DateUtil.formatDateTime(meetingDetail.getOrderEndTime()));
        }
        //????????????????????????
        if (OrderConstant.OrderType.LONG_RENT_ORDER_MEETING.equals(meetingDetail.getOrderType())) {
            orderNoticeDTO.setOrderStartTime(DateUtil.formatDate(meetingDetail.getOrderStartTime()));
            orderNoticeDTO.setOrderEndTime(DateUtil.formatDate(meetingDetail.getOrderEndTime()));
        }
        try {
            orderNoticeDTO.setOrderTime(twoTimeDiffer(meetingDetail.getOrderStartTime(), meetingDetail.getOrderEndTime()));
        } catch (ParseException e) {
            throw BusinessException.of("??????????????????");
        }
        orderNoticeDTO.setApproveState(approveState);
        orderNoticeDTO.setNoticeContent(noticeContent);
        orderNoticeDTO.setBuildName(meetingDetail.getBuildName());
        orderNoticeDTO.setFloorNum(meetingDetail.getFloorNum());
        orderNoticeDTO.setRoomId(meetingDetail.getRoomId());
        orderNoticeDTO.setRoomName(meetingDetail.getRoomName());
        orderNoticeDTO.setRoomNum(meetingDetail.getRoomNum());
        orderNoticeDTO.setSeatNum(null);
        return orderNoticeDTO;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param longDetail
     * @return
     */
    public static OrderNoticeDTO getOrderNoticeMeetingDTOBySeatList(StaLongRepVO longDetail, String noticeContent, String approveState, Integer orderType) {
        OrderNoticeDTO orderNoticeDTO = new OrderNoticeDTO();
        orderNoticeDTO.setOrderListId(longDetail.getOrderSeatId());
        orderNoticeDTO.setOrderMeetingInfoId(null);
        orderNoticeDTO.setUserName(longDetail.getUserName());
        orderNoticeDTO.setOrderType(orderType);
//        orderNoticeDTO.setOrderNo(null);
        orderNoticeDTO.setTheme(null);
//        orderNoticeDTO.setListState(longDetail.getListState());
        orderNoticeDTO.setOrderStartTime(longDetail.getOrderStartTime());
        orderNoticeDTO.setOrderEndTime(longDetail.getOrderEndTime());
        try {
            orderNoticeDTO.setOrderTime(twoTimeDiffer(longDetail.getOrderStartTime(), longDetail.getOrderEndTime()));
        } catch (ParseException e) {
            throw BusinessException.of("??????????????????");
        }
        orderNoticeDTO.setApproveState(approveState);
        orderNoticeDTO.setNoticeContent(noticeContent);
        orderNoticeDTO.setBuildName(longDetail.getBuildName());
        orderNoticeDTO.setFloorNum(longDetail.getFloorNum());
        orderNoticeDTO.setRoomId(null);
        orderNoticeDTO.setRoomName(longDetail.getRoomName());
        orderNoticeDTO.setRoomNum(longDetail.getRoomNum());
        orderNoticeDTO.setSeatNum(longDetail.getSeatNum());
        return orderNoticeDTO;
    }

    public static OrderMeetingShortDetailRepVO getShortDetailRepVoByMeetingDetailDTO(OrderMeetingDetailDTO meetingDetailDTO, List<OrderMeetingRentDetail> rentDetailList) {
        OrderMeetingShortDetailRepVO orderMeetingShortDetailRepVO = new OrderMeetingShortDetailRepVO();
        orderMeetingShortDetailRepVO.setOrderMeetingId(meetingDetailDTO.getOrderMeetingId());
        orderMeetingShortDetailRepVO.setUserId(meetingDetailDTO.getUserId());
        orderMeetingShortDetailRepVO.setUserName(meetingDetailDTO.getUserName());
        orderMeetingShortDetailRepVO.setUserPhone(meetingDetailDTO.getUserPhone());
        orderMeetingShortDetailRepVO.setUserType(meetingDetailDTO.getUserType());
        orderMeetingShortDetailRepVO.setUserNo(meetingDetailDTO.getUserNo());
        orderMeetingShortDetailRepVO.setUserOrgName(formatOrgName(meetingDetailDTO.getUserOrgName()));
        orderMeetingShortDetailRepVO.setBuildName(meetingDetailDTO.getBuildName());
        orderMeetingShortDetailRepVO.setFloorName(meetingDetailDTO.getFloorName());
        orderMeetingShortDetailRepVO.setRoomName(meetingDetailDTO.getRoomName());
        orderMeetingShortDetailRepVO.setOrderStartTime(DateUtil.formatDateTime(meetingDetailDTO.getOrderStartTime()));
        orderMeetingShortDetailRepVO.setOrderEndTime(DateUtil.formatDateTime(meetingDetailDTO.getOrderEndTime()));
        orderMeetingShortDetailRepVO.setTimeShow(orderMeetingShortDetailRepVO.getOrderStartTime() + " ~ " + orderMeetingShortDetailRepVO.getOrderEndTime());
        orderMeetingShortDetailRepVO.setUseTime(formatLongTime(meetingDetailDTO.getUseTime()) + "h");
        for (OrderConstant.MeetingStateEnum value : OrderConstant.MeetingStateEnum.values()) {
            if (meetingDetailDTO.getState().equals(value.getAftId())) {
                orderMeetingShortDetailRepVO.setRemark(value.getRemark()).setState(value.getBefId());
            }
        }
        orderMeetingShortDetailRepVO.setRemark(StringUtils.isNotBlank(meetingDetailDTO.getRemark()) ? meetingDetailDTO.getRemark() : "??????");
        orderMeetingShortDetailRepVO.setSignCount(rentDetailList.size());
        orderMeetingShortDetailRepVO.setSignPeople(Collections.emptyList());
        if (CollectionUtil.isNotEmpty(rentDetailList)) {
            orderMeetingShortDetailRepVO.setSignPeople(rentDetailList.stream().map(OrderMeetingRentDetail::getUserName).collect(Collectors.toList()));
        }
        return orderMeetingShortDetailRepVO;
    }

    public static OrderMeetingLongDetailRepVO getLongDetailRepVo(OrderMeetingDetailDTO meetingDetailDTO, List<OrderMeetingInfo> orderMeetingInfoList, List<OrderMeetingRentDetail> rentDetailList) {
        OrderMeetingLongDetailRepVO orderMeetingLongDetailRepVO = new OrderMeetingLongDetailRepVO();
        orderMeetingLongDetailRepVO.setOrderMeetingId(meetingDetailDTO.getOrderMeetingId());
        orderMeetingLongDetailRepVO.setUserId(meetingDetailDTO.getUserId());
        orderMeetingLongDetailRepVO.setUserName(meetingDetailDTO.getUserName());
        orderMeetingLongDetailRepVO.setUserPhone(meetingDetailDTO.getUserPhone());
        orderMeetingLongDetailRepVO.setUserType(meetingDetailDTO.getUserType());
        orderMeetingLongDetailRepVO.setUserNo(meetingDetailDTO.getUserNo());
        orderMeetingLongDetailRepVO.setUserOrgName(formatOrgName(meetingDetailDTO.getUserOrgName()));
        orderMeetingLongDetailRepVO.setBuildName(meetingDetailDTO.getBuildName());
        orderMeetingLongDetailRepVO.setFloorName(meetingDetailDTO.getFloorName());
        orderMeetingLongDetailRepVO.setRoomName(meetingDetailDTO.getRoomName());
        orderMeetingLongDetailRepVO.setOrderStartTime(DateUtil.formatDate(meetingDetailDTO.getOrderStartTime()));
        orderMeetingLongDetailRepVO.setOrderEndTime(DateUtil.formatDate(meetingDetailDTO.getOrderEndTime()));
        orderMeetingLongDetailRepVO.setTimeShow(orderMeetingLongDetailRepVO.getOrderStartTime() + " ~ " + orderMeetingLongDetailRepVO.getOrderEndTime());
        for (OrderConstant.MeetingStateEnum value : OrderConstant.MeetingStateEnum.values()) {
            if (meetingDetailDTO.getState().equals(value.getAftId())) {
                orderMeetingLongDetailRepVO.setRemark(value.getRemark()).setState(value.getBefId());
            }
        }
        orderMeetingLongDetailRepVO.setUseTime(formatLongTime(meetingDetailDTO.getUseTime()) + "h");
        orderMeetingLongDetailRepVO.setSignList(getSignList(orderMeetingInfoList, rentDetailList));
        return orderMeetingLongDetailRepVO;
    }

    /**
     * ??????????????????
     *
     * @param orderMeetingInfoList
     * @param rentDetailList
     * @return
     */
    private static List<OrderMeetingLongSignRepVO> getSignList(List<OrderMeetingInfo> orderMeetingInfoList, List<OrderMeetingRentDetail> rentDetailList) {
        ArrayList<OrderMeetingLongSignRepVO> list = new ArrayList<>();
        Map<String, List<OrderMeetingRentDetail>> rentDetailListMap = new HashMap<>();
        if (ObjectUtil.isNotNull(rentDetailList)) {
            rentDetailListMap = rentDetailList.stream().collect(Collectors.groupingBy(OrderMeetingRentDetail::getOrderMeetingInfoId));
        }
        for (OrderMeetingInfo orderMeetingInfo : orderMeetingInfoList) {
            OrderMeetingLongSignRepVO signRepVO = new OrderMeetingLongSignRepVO();
            signRepVO.setOrderMeetingInfoId(orderMeetingInfo.getOrderMeetingInfoId());
            signRepVO.setUserStartTime(DateUtil.formatDateTime(orderMeetingInfo.getUserStartTime()));
            signRepVO.setUserEndTime(DateUtil.formatDateTime(orderMeetingInfo.getUseEndTime()));
            //??????????????????????????????????????????
            List<OrderMeetingRentDetail> rentDetailListForMeetingInfo = rentDetailListMap.get(orderMeetingInfo.getOrderMeetingInfoId());
            List<String> signPeople = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(rentDetailListForMeetingInfo)) {
                signPeople = rentDetailListForMeetingInfo.stream().map(OrderMeetingRentDetail::getUserName).collect(Collectors.toList());
            }

            signRepVO.setSignPeople(signPeople);
            list.add(signRepVO);
        }
        return list;
    }

    /**
     * ????????????: <br>
     * ????????????????????????????????????????????????,??????????????????????????????
     *
     * @Param: [userId]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/8 11:35
     */
    public void checkUserIsExist(String userId) {
        // ??????????????????????????????????????????????????????
        LambdaQueryWrapper<OrderUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.select(OrderUser::getOrderUserId).eq(OrderUser::getUserId, userId);
        Integer isUserExit = orderUserMapper.selectCount(userQueryWrapper);

        if (isUserExit == 0) {
            // ?????????????????????
            OrderUser orderUser = new OrderUser().setUserId(userId).setLearnMaxTime(0).setWeekLearnTime(0).setLearnTotalTime(0)
                    .setInBlacklistCount(0).setDelFlag("0").setOrderCount(1);
            orderUserMapper.insert(orderUser);
        }

    }

    /**
     * ????????????: <br>
     * ????????????????????????????????????????????????,??????????????????????????????
     *
     * @Param: [userId]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/8 11:35
     */
    public void addOrUpdateOrderUser(String userId, OrderMeetingList orderMeetingList,String orgId) {
        // ??????????????????????????????????????????????????????
        LambdaQueryWrapper<OrderUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(OrderUser::getUserId, userId);
        OrderUser orderUserSelect = orderUserMapper.selectOne(userQueryWrapper);

        //?????? ??????
        if (ObjectUtil.isNotNull(orderMeetingList)) {
            if (ObjectUtil.isNull(orderUserSelect)) {
                // ?????????????????????
                OrderUser orderUser = new OrderUser().setUserId(userId).setLearnMaxTime(0).setWeekLearnTime(0).setLearnTotalTime(Math.toIntExact(orderMeetingList.getUseTime() / 1000))
                        .setInBlacklistCount(0).setDelFlag("0").setOrderCount(1).setOrgId(orgId);
                orderUserMapper.insert(orderUser);
            } else {
                //?????????????????????????????????
                LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                userLambdaUpdateWrapper.set(OrderUser::getOrderCount, orderUserSelect.getOrderCount() + 1)
                        .eq(OrderUser::getUserId, userId);
                orderUserMapper.update(null, userLambdaUpdateWrapper);
            }
        }

    }

    /**
     * ????????????: <br>
     * ???????????????????????????
     *
     * @Param: [userId]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/8 11:35
     */
    public void updateOrderUserLearnTotalTime(String userId, Long time) {
        // ??????????????????????????????????????????????????????
        LambdaQueryWrapper<OrderUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(OrderUser::getUserId, userId);
        OrderUser orderUserSelect = orderUserMapper.selectOne(userQueryWrapper);
        if (ObjectUtil.isNotNull(orderUserSelect)) {
            //?????????????????????????????????
            LambdaUpdateWrapper<OrderUser> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userLambdaUpdateWrapper.set(OrderUser::getLearnTotalTime, orderUserSelect.getLearnTotalTime() + time / 1000)
                    .set(OrderUser::getUpdateTime, new Date())
                    .eq(OrderUser::getUserId, userId);
            orderUserMapper.update(null, userLambdaUpdateWrapper);
        }
    }

    /**
     * @return java.lang.String
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/5/11 14:16
     * @params [date]
     */
    public String createListNo(Date date, String tenantCode) {

        // ?????????????????????????????????
        String redisKey = tenantCode + ":order:totalOrderCount";

        // ????????????????????????
        Integer restTime = Convert.toInt(DateUtil.betweenMs(date, DateUtil.endOfDay(date).toJdkDate()) / 1000L);
        Integer count = redisRepository.get(redisKey, Integer.class);
        if (Objects.isNull(count)) {
            redisRepository.set(redisKey, 1, restTime);
            count = 1;
        }

        redisRepository.increasing(redisKey, 1);

        // ??????????????????
        DecimalFormat decimalFormat = new DecimalFormat("000000");
        String listNo = StrUtil.toString(DatePattern.PURE_DATE_FORMAT.format(date)) + decimalFormat.format(count);
        return listNo;
    }

    /**
     * @return java.lang.Long
     * @description: ???????????????????????????????????????24???,???????????????
     * @author: lc
     * @date: 2021/4/25 13:41
     * @params []
     */
    public static Long getTodayRestTime(Date date) {

        // ????????????23:59:59??????
        Date lastDate = DateUtil.endOfDay(new Date()).toJdkDate();

        // ????????????????????????
        return DateUtil.betweenMs(date, lastDate) / 1000L;

    }

    /**
     * @return java.lang.Double
     * @description:????????????????????????????????????(h)
     * @author: lc
     * @date: 2021/4/20 16:03
     * @params [timeBef, timeAft]
     */
    public static String twoTimeDiffer(String timeBef, String timeAft) throws ParseException {
        Date parse = DatePattern.NORM_DATETIME_FORMAT.parse(timeAft);
        Date parse2 = DatePattern.NORM_DATETIME_FORMAT.parse(timeBef);
        long l = DateUtil.betweenMs(parse, parse2);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double a = l / (1000 * 3600.0);
        return decimalFormat.format(a);
    }

    /**
     * @return java.lang.Double
     * @description:????????????????????????????????????(h)
     * @author: lc
     * @date: 2021/4/20 16:03
     * @params [timeBef, timeAft]
     */
    public static String twoTimeDiffer(Date parse, Date parse2) throws ParseException {
        long l = DateUtil.betweenMs(parse, parse2);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double a = l / (1000 * 3600.0);
        return decimalFormat.format(a);
    }

    /**
     * ????????????????????????
     * @param time
     * @return
     */
    public static String formatLongTime(Integer time) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double a = (double)time / (1000 * 3600.0);
        return decimalFormat.format(a);
    }

    /**
     * ?????????????????????
     * @param time
     * @return
     */
    public static String formatShortTime(Integer time) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double a = (double)time / (3600.0);
        return decimalFormat.format(a);
    }

    /**
     * ????????????: <br>
     * ??????????????????????????????
     *
     * @Param: [userId]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/9 17:47
     */
    public void handleCancel(String userId) {

        LambdaQueryWrapper<OrderUser> queryWrapper = new LambdaQueryWrapper<>();
        LambdaUpdateWrapper<OrderUser> updateWrapper = new LambdaUpdateWrapper<>();

        // ????????????????????????
        queryWrapper.eq(OrderUser::getUserId, userId);
        OrderUser orderUser = orderUserMapper.selectOne(queryWrapper);
        CommonUtil.notNull(orderUser, "??????????????????");
        Integer cancelCount = orderUser.getCancelCount();

        // ??????????????????
        if (StrUtil.isEmptyIfStr(cancelCount)) {
            updateWrapper.set(OrderUser::getCancelCount, 1)
                    .eq(OrderUser::getUserId, userId);
        } else {
            updateWrapper.set(OrderUser::getCancelCount, cancelCount + 1)
                    .eq(OrderUser::getUserId, userId);
        }

        orderUserMapper.update(null, updateWrapper);

    }

    /**
     * ????????????: <br>
     * ???????????????????????????????????????????????????????????????
     *
     * @Param: [orderMeetingVO]
     * @Return: com.eseasky.modules.space.vo.request.QueryOrderRoomParam
     * @Author: ?????????
     * @Date: 2021/6/15 11:26
     */
    public static QueryOrderRoomParam getQueryOrderRoomParam(OrderMeetingVO orderMeetingVO) {
        QueryOrderRoomParam param = new QueryOrderRoomParam();
        param.setOrderType(orderMeetingVO.getOrderType());
        param.setRoomId(orderMeetingVO.getRoomId());
        param.setStartDate(orderMeetingVO.getOrderStartTime());
        param.setEndDate(orderMeetingVO.getOrderEndTime());
        return param;
    }

    /**
     * ????????????: <br>
     * ????????? room ???????????????
     *
     * @Param: [roomId]
     * @Return: com.eseasky.modules.space.entity.SpaceConf
     * @Author: ?????????
     * @Date: 2021/6/15 11:35
     */
    public SpaceConfVO getRoomConf(String roomId) {
        R<?> confRes = spaceRoomService.getRoomConfForOrder(roomId);
        CommonUtil.check(R.SUCCESS_CODE == confRes.getCode(), confRes.getMsg());
        return (SpaceConfVO) confRes.getData();
    }

    /**
     * ????????????: <br>
     * ????????????????????????
     *
     * @Param: [orderMeetingVO]
     * @Return: com.eseasky.modules.space.vo.SpaceRoomVO
     * @Author: ?????????
     * @Date: 2021/6/15 11:36
     */
    public OneBOneFOneR getRoomInfoByOrder(OrderMeetingVO orderMeetingVO) {
        //??????room ??????
        QueryOrderRoomParam param = OrderUtils.getQueryOrderRoomParam(orderMeetingVO);
        R<?> checkRes = spaceRoomService.getRoomInfoForOrder(param);
        CommonUtil.check(R.SUCCESS_CODE == checkRes.getCode(), checkRes.getMsg());
        return (OneBOneFOneR) checkRes.getData();
    }

    /**
     * ????????????: <br>
     * ????????????????????????????????????
     *
     * @Param: [orderMeetingVO, userId, tenantCode, conf]
     * @Return: void
     * @Author: ?????????
     * @Date: 2021/6/15 15:58
     */
    public void checkAppointProperties(OrderMeetingVO orderMeetingVO, String userId, String tenantCode, SpaceConfVO conf, Integer orderType) {
        // ????????????????????????????????????????????????
        Integer subLimitCount = conf.getSubLimitCount();
        Integer subCancelCount = conf.getSubCancelCount();

        //???????????????????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            Long appointTime = (orderMeetingVO.getOrderEndTime().getTime() - orderMeetingVO.getOrderStartTime().getTime()) / 1000;
            Integer subMinTime = conf.getSubMinTime();
            if (null != subMinTime) {
                CommonUtil.check(appointTime >= subMinTime * 60, "?????????????????????????????????" + subMinTime + "??????");
            }
            Integer subMaxTime = conf.getSubMaxTime();
            if (null != subMaxTime) {
                CommonUtil.check(appointTime <= subMaxTime * 60, "?????????????????????????????????" + subMaxTime + "??????");
            }
        }
        //??????????????????
        Integer meetingJoinCount = conf.getMeetingJoinCount();
        if (null != meetingJoinCount) {
            Integer peopleCount = CollectionUtil.isNotEmpty(orderMeetingVO.getAttendMeetingPeople()) ? orderMeetingVO.getAttendMeetingPeople().size() : 0;
            CommonUtil.check(peopleCount <= meetingJoinCount, "??????????????????????????????????????????,??????????????????:" + meetingJoinCount);
        }

        // ?????????????????????????????????????????????????????????????????????1,??????24?????????
        Long todayRestTime = OrderUtils.getTodayRestTime(new Date());
        String userSubCountKey = tenantCode + ":order:userSubCount:" + userId;
        Integer orderCount = redisRepository.get(userSubCountKey, Integer.class);
        if (Objects.isNull(orderCount)) {
            redisRepository.set(userSubCountKey, 0, todayRestTime);
            orderCount = 0;
        }

        // ???????????????????????????????????????????????????????????????????????????0
        String userCanCelCountKey = tenantCode + ":order:userCancelCount:" + userId;
        Integer cancelCount = redisRepository.get(userCanCelCountKey, Integer.class);
        if (Objects.isNull(cancelCount)) {
            redisRepository.set(userCanCelCountKey, 0, todayRestTime);
            cancelCount = 0;
        }

        // ??????????????????????????????????????????????????????????????????????????????????????????
        if (null != subLimitCount) {
            if (subLimitCount <= orderCount && orderCount != -1) {
                throw BusinessException.of("????????????????????????????????????,????????????");
            }
        }
        if (null != subCancelCount) {
            if (subCancelCount <= cancelCount && cancelCount != -1) {
                throw BusinessException.of("????????????????????????????????????????????????");
            }
        }
    }

    /**
     * ????????????id????????????????????????
     *
     * @param userIds
     * @return
     */
    public List<SysUser> getSysUserListByUserIds(List<String> userIds) {
        List<SysUser> sysUsers = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userIds)) {
            LambdaQueryWrapper<SysUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(SysUser::getId, userIds);
            sysUsers = sysUserService.list(userLambdaQueryWrapper);
        }
        return sysUsers;
    }

    /**
     * ????????????????????????????????????id??????
     *
     * @param attendMeetingPeople
     * @return
     */
    public List<String> getUserIds(String attendMeetingPeople, String orderMeetingInfoId) {
        List<String> userIds = new ArrayList<>();
        if (StringUtils.isNotBlank(orderMeetingInfoId)) {
            OrderMeetingInfo latestMeetingInfo = orderMeetingInfoMapper.getOrderMeetingInfoByInfoId(orderMeetingInfoId);
            if (!ObjectUtil.isNull(latestMeetingInfo)) {
                LambdaQueryWrapper<OrderMeetingUser> meetingUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
                //???????????????????????????id
                meetingUserLambdaQueryWrapper.eq(OrderMeetingUser::getOrderMeetingInfoId, latestMeetingInfo.getOrderMeetingInfoId());
                List<OrderMeetingUser> meetingUserList = orderMeetingUserMapper.selectList(meetingUserLambdaQueryWrapper);
                if (CollectionUtil.isNotEmpty(meetingUserList)) {
                    userIds = meetingUserList.stream().map(OrderMeetingUser::getUserId).collect(Collectors.toList());
                }
            } else {
                userIds = CommonUtil.stringToList(attendMeetingPeople);
            }
        } else {
            userIds = CommonUtil.stringToList(attendMeetingPeople);
        }

        return userIds;
    }

    /**
     * ????????????????????????????????? ????????????
     *
     * @param sysUserDTO
     * @param orderMeetingList
     * @param meetingListDetailRepVO
     * @return
     */
    public static JSONObject getMeetingAppointSuccessWxMsg(SysUserDTO sysUserDTO, OrderMeetingList orderMeetingList, OrderMeetingListDetailRepVO meetingListDetailRepVO) {
        JSONObject jsonObject = new JSONObject();
        //?????????openId
        jsonObject.put("touser", sysUserDTO.getOpenid());
        //??????????????????????????????id
        jsonObject.put("template_id", OrderConstant.NoticeTemplateId.APPOINTMENT_SUCCESS);
        //???????????????????????????????????????????????????????????????????????????????????????,?????????index?foo=bar???????????????????????????????????????
        jsonObject.put("page", null);
        jsonObject.put("miniprogram_state", "developer");
        jsonObject.put("lang", "zh_CN");
        JSONObject data = new JSONObject();
        //??????
        data.put("name8", sysUserDTO.getUsername());
        //????????????
        data.put("date2", DateUtil.formatDateTime(orderMeetingList.getOrderStartTime()));
        //??????
        data.put("thing10", meetingListDetailRepVO.getBuildName() + "-" + meetingListDetailRepVO.getFloorNum() + "-" + meetingListDetailRepVO.getRoomName() + "-" + meetingListDetailRepVO.getRoomNum());
        //????????????
        data.put("thing29", meetingListDetailRepVO.getRoomNum());
        //?????????
        data.put("character_string15", orderMeetingList.getOrderMeetingId());
        jsonObject.put("data", data);
        return jsonObject;
    }

    /**
     * ???????????????????????????
     *
     * @param user
     * @param orderMeetingList
     * @return
     */
    public JSONObject getClockWxMsg(SysUser user, OrderMeetingList orderMeetingList, String title) {
        JSONObject jsonObject = new JSONObject();
        //?????????openId
        jsonObject.put("touser", user.getOpenid());
        //??????????????????????????????id
        jsonObject.put("template_id", OrderConstant.NoticeTemplateId.CLOCK_REMINDER);
        //???????????????????????????????????????????????????????????????????????????????????????,?????????index?foo=bar???????????????????????????????????????
        jsonObject.put("page", OrderConstant.wxPage);
        jsonObject.put("miniprogram_state", wxProperties.getMiniprogram_state());
        jsonObject.put("lang", "zh_CN");
        JSONObject data = new JSONObject();
        //????????????
        JSONObject thing4 = new JSONObject();
        thing4.put("value", title);
        data.put("thing4", thing4);
        //?????????
        JSONObject thing6 = new JSONObject();
        thing6.put("value", user.getUsername());
        data.put("thing6", thing6);
        //??????????????????
        JSONObject time2 = new JSONObject();
        time2.put("value", DateUtil.formatDateTime(orderMeetingList.getOrderStartTime()));
        data.put("time2", time2);
        //??????
        JSONObject thing5 = new JSONObject();
        thing5.put("value", "????????????????????????????????????????????????????????????");
        data.put("thing5", thing5);
        jsonObject.put("data", data);
        return jsonObject;
    }

    /**
     * ??????????????????????????? ????????????
     *
     * @param userName
     * @param openId
     * @param orderStartTime
     * @return
     */
    public JSONObject getClockWxMsg(String userName, String openId, Date orderStartTime, String title) {
        JSONObject jsonObject = new JSONObject();
        //?????????openId
        jsonObject.put("touser", openId);
        //??????????????????????????????id
        jsonObject.put("template_id", OrderConstant.NoticeTemplateId.CLOCK_REMINDER);
        //???????????????????????????????????????????????????????????????????????????????????????,?????????index?foo=bar???????????????????????????????????????
        jsonObject.put("page", OrderConstant.wxPage);
        jsonObject.put("miniprogram_state", wxProperties.getMiniprogram_state());
        jsonObject.put("lang", "zh_CN");
        JSONObject data = new JSONObject();
        //????????????
        JSONObject thing4 = new JSONObject();
        thing4.put("value", title);
        data.put("thing4", thing4);
        //?????????
        JSONObject thing6 = new JSONObject();
        thing6.put("value", userName);
        data.put("thing6", thing6);
        //??????????????????
        JSONObject time2 = new JSONObject();
        time2.put("value", DateUtil.formatDateTime(orderStartTime));
        data.put("time2", time2);
        //??????
        JSONObject thing5 = new JSONObject();
        thing5.put("value", "????????????????????????????????????????????????????????????");
        data.put("thing5", thing5);
        jsonObject.put("data", data);
        return jsonObject;
    }

    /**
     * ????????????????????????
     */
    public void sendNoticeToAppointmentPeople(String attendMeetingPeople, String orderMeetingInfoId, OrderMeetingList orderMeetingList) {
        List<String> userIds = getUserIds(attendMeetingPeople, orderMeetingInfoId);
        ArrayList<OrderNotice> orderNotices = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userIds)) {
            //???????????????????????????
            userIds.remove(orderMeetingList.getUserId());
            for (String userId : userIds) {
                OrderMeetingListDetailRepVO meetingListDetailRepVO = orderMeetingListMapper.getOrderDetail(orderMeetingList.getOrderMeetingId());
                OrderNoticeDTO orderNoticeMeetingDTO = getOrderNoticeDTOByMeetingList(meetingListDetailRepVO, "???????????????", null);

                OrderNotice noticeApp = OrderNoticeUtils.getOrderNotice(userId, OrderConstant.NoticeType.App, OrderConstant.NoticeTitleType.MEETING, OrderConstant.NoticeContentType.INVITE, "?????????????????????", JSONObject.toJSONString(orderNoticeMeetingDTO));
                orderNotices.add(noticeApp);
            }
        }
        if (CollectionUtil.isNotEmpty(orderNotices)) {
            //??????app????????????
            orderNoticeService.saveBatch(orderNotices);
        }
    }

    /**
     * ????????????????????????
     */
    public void sendNoticeToAppointmentPeople(String attendMeetingPeople, String orderMeetingInfoId, OrderMeetingListDetailRepVO meetingListDetailRepVO) {
        List<String> userIds = getUserIds(attendMeetingPeople, orderMeetingInfoId);
        ArrayList<OrderNotice> orderNotices = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userIds)) {
            //???????????????????????????
            userIds.remove(meetingListDetailRepVO.getUserId());
            for (String userId : userIds) {
                OrderNoticeDTO orderNoticeMeetingDTO = getOrderNoticeDTOByMeetingList(meetingListDetailRepVO, "???????????????", null);

                OrderNotice noticeApp = OrderNoticeUtils.getOrderNotice(userId, OrderConstant.NoticeType.App, OrderConstant.NoticeTitleType.MEETING, OrderConstant.NoticeContentType.INVITE, "?????????????????????", JSONObject.toJSONString(orderNoticeMeetingDTO));
                orderNotices.add(noticeApp);
            }
        }
        if (CollectionUtil.isNotEmpty(orderNotices)) {
            //??????app????????????
            orderNoticeService.saveBatch(orderNotices);
        }
    }

    /**
     * ????????????????????????
     */
    public static List<String> formatOrgName(String str) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotBlank(str)) {
            list = Arrays.asList(str.split("/")).stream().map(String::trim).collect(Collectors.toList());
        }
        return list;
    }
}
