package com.eseasky.modules.order.service.impl;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.ExcelUtils;
import com.eseasky.common.code.utils.R;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.ListExcelDTO;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.mapper.OrderGroupDetailMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.service.OrderStatisticsService;
import com.eseasky.modules.order.vo.request.AnaRoomReqVO;
import com.eseasky.modules.order.vo.request.OrderListReqVO;
import com.eseasky.modules.order.vo.request.StaSpcReqVO;
import com.eseasky.modules.order.vo.request.StaUserReqVO;
import com.eseasky.modules.order.vo.response.*;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.mapper.SpaceBuildMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @describe:
 * @title: OrderStatisticsServiceImpl
 * @Author lc
 * @Date: 2021/5/21
 */
@Service
@Slf4j
@Transactional
public class OrderStatisticsServiceImpl implements OrderStatisticsService {


    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    SpaceBuildService buildService;

    @Autowired
    private SpaceUtil spaceUtil;

    @Autowired
    private OrderGroupDetailMapper orderGroupDetailMapper;


    private HashSet shortRent = Sets.newHashSet(1, 3, 5);
    private HashSet longRent = Sets.newHashSet(2, 4);

    /**
     * @return R
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/5/20 17:42
     * @params []
     */
    public R showSpaceStatistics(StaSpcReqVO staSpcReqVO) {

        // ??????????????????
        Integer orderType = staSpcReqVO.getOrderType();

        // ????????????????????????????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT.equals(orderType)) {
            Page shortStatics = getShortStatics(staSpcReqVO);
            return R.ok(shortStatics);
        }

        // ??????????????????????????????????????????????????????
        if (OrderConstant.OrderType.LONG_RENT.equals(orderType)) {
            Page longStatics = getLongStatics(staSpcReqVO);
            return R.ok(longStatics);
        }

        // ????????????????????????????????????????????????
        if (OrderConstant.OrderType.GROUP_RENT.equals(orderType)) {
            Page shortStatics = getGroupStatics(staSpcReqVO);
            return R.ok(shortStatics);
        }

        // ??????????????????????????????????????????????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            Page longStatics = getMeetShortStatics(staSpcReqVO);
            return R.ok(longStatics);
        }

        // ????????????????????????
        else {
            Page longStatics = getMeetLongStatics(staSpcReqVO);
            return R.ok(longStatics);
        }

    }

    /**
     * @return
     * @description: ??????????????????????????????????????????
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getShortStatics(StaSpcReqVO staSpcReqVO) {
        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());
        // ??????????????????????????????
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // ????????????????????????
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceShortSta(page, staSpcReqVO);

        // ??????????????????
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            String aveCount = decimalFormat.format(spaceStatistic.getTotalCount() / days);
            String totTime = parseHour(spaceStatistic.getTotalTime());
            String aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // ????????????????????????
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: ?????????????????????????????????
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getLongStatics(StaSpcReqVO staSpcReqVO) {

        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());

        // ????????????????????????
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceLongSta(page, staSpcReqVO);

        // ??????????????????????????????
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // ??????????????????
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            Integer totalCount = spaceStatistic.getTotalCount();
            String totalTime = spaceStatistic.getTotalTime();
            // ??????????????????0?????????????????????
            String aveCount = "0";
            if (totalCount != 0) {
                aveCount = decimalFormat.format(totalCount / days);
            }

            String aveTime = "0";
            String totTime = parseHour(totalTime);
            if (!totalTime.equals("0")) {
                aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            }
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // ????????????????????????
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: ??????????????????????????????
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getGroupStatics(StaSpcReqVO staSpcReqVO) {
        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());
        // ??????????????????????????????
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // ????????????????????????
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceGroupSta(page, staSpcReqVO);

        // ??????????????????
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            String aveCount = decimalFormat.format(spaceStatistic.getTotalCount() / days);
            String totTime = parseHour(spaceStatistic.getTotalTime());
            String aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // ????????????????????????
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: ?????????????????????????????????
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getMeetShortStatics(StaSpcReqVO staSpcReqVO) {

        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());

        // ????????????????????????
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceMeetShortSta(page, staSpcReqVO);

        // ??????????????????????????????
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // ??????????????????
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            String aveCount = decimalFormat.format(spaceStatistic.getTotalCount() / days);
            String totTime = parseHour(spaceStatistic.getTotalTime());
            String aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // ????????????????????????
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: ?????????????????????????????????
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getMeetLongStatics(StaSpcReqVO staSpcReqVO) {

        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());

        // ????????????????????????
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceMeetLongSta(page, staSpcReqVO);

        // ??????????????????????????????
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // ??????????????????
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            Integer totalCount = spaceStatistic.getTotalCount();
            String totalTime = spaceStatistic.getTotalTime();
            // ??????????????????0?????????????????????
            String aveCount = "0";
            if (totalCount != 0) {
                aveCount = decimalFormat.format(totalCount / days);
            }

            String aveTime = "0";
            String totTime = parseHour(totalTime);
            if (!totalTime.equals("0")) {
                aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            }
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // ????????????????????????
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/5/24 16:28
     * @params [roomId]
     */
    public R showSpaceAnalysis(AnaRoomReqVO anaRoomReqVO) {

        // ??????????????????
        Integer orderType = anaRoomReqVO.getOrderType();

        // ??????????????????
        ArrayList<SpaceAnalysisRepVO> spaceAnalysis = null;
        // ????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT.equals(orderType)) {
            spaceAnalysis = orderSeatListMapper.getShortAnalysis(anaRoomReqVO);
        }
        // ??????????????????
        if (OrderConstant.OrderType.GROUP_RENT.equals(orderType)) {
            spaceAnalysis = orderSeatListMapper.getGroupAnalysis(anaRoomReqVO);
        }
        // ????????????????????????
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            spaceAnalysis = orderSeatListMapper.getMeetingAnalysis(anaRoomReqVO);
        }

        return R.ok(spaceAnalysis);
    }

    /**
     * @return R
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/5/20 17:42
     * @params []
     */
    @Override
    public R showUserStatistics(StaUserReqVO staUserReqVO) {

        Page<UserStatisticsRepVO> page = new Page<>(staUserReqVO.getPageNum(), staUserReqVO.getPageSize());

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // ????????????????????????
        ArrayList<UserStatisticsRepVO> userStatistics = orderSeatListMapper.getUserStatistics(page, staUserReqVO);
        for (UserStatisticsRepVO userStatistic : userStatistics) {
            String orderCount = userStatistic.getOrderCount();
            String zero = "0";
            // ?????????????????????0????????????????????????
            if (!zero.equals(orderCount)) {
                String learnTotalTime = userStatistic.getLearnTotalTime();
                String totalTime = parseHour(learnTotalTime);
                userStatistic.setLearnTotalTime(totalTime);
                String format = decimalFormat.format(Convert.toDouble(totalTime) / Convert.toDouble(orderCount));
                userStatistic.setLearnAverageTime(format);
            }
            userStatistic.setLearnAverageTime("0");
        }

        page.setRecords(userStatistics);
        return R.ok(page);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/5/25 15:30
     * @params [userId]
     */
    @Override
    public R showUserAnalysis(String userId) {
        ArrayList<UserAnalysisRepVO> userAnalysis = orderSeatListMapper.getUserAnalysis(userId);
        return R.ok(userAnalysis);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: ??????????????????
     * @author: lc
     * @date: 2021/4/16 16:22
     * @params [OrderListReqVO]
     */
    @Override
    public R showOrderList(OrderListReqVO orderListReqVO) {

        //??????????????????
        Integer orderType = orderListReqVO.getOrderType();

        // ?????????????????????????????????????????????
        Page<OrderListRepVO> page = new Page<>(orderListReqVO.getPageNum(), orderListReqVO.getPageSize());
        List<OrderListRepVO> lists = Lists.newArrayList();
        // ???????????????????????????????????????????????????
        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT) || orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            lists = orderSeatListMapper.getBuildSingleList(page, orderListReqVO);
        }
        // ???????????????????????????????????????????????????
        if (orderType.equals(OrderConstant.OrderType.GROUP_RENT)) {
            lists = orderSeatListMapper.getBuildGroupList(page, orderListReqVO);
        }
        // ???????????????????????????????????????????????????
        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING) || orderType.equals(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING)) {
            lists = orderSeatListMapper.getBuildMeetingList(page, orderListReqVO);
        }

        // ????????????????????????
        for (OrderListRepVO list : lists) {
            // ????????????????????????
            list.setArea(list.getBuildName() + list.getFloorNum() + "F" + list.getRoomName() + list.getRoomNum());
        }

        // ????????????????????????
        // ??????
        if (shortRent.contains(orderType)) {
            for (OrderListRepVO list : lists) {
                String showTime = StrUtil.subWithLength(list.getOrderStartTime(), 0, 16) + "~" +
                        StrUtil.subWithLength(list.getOrderEndTime(), 0, 16);
                list.setShowTime(showTime);
            }
        }
        // ??????
        if (longRent.contains(orderType)) {
            for (OrderListRepVO list : lists) {
                String showTime = StrUtil.subWithLength(list.getOrderStartTime(), 0, 10) + "~" +
                        StrUtil.subWithLength(list.getOrderEndTime(), 0, 10);
                list.setShowTime(showTime);
            }
        }

        page.setRecords(lists);
        return R.ok(page);
    }


    /**
     * @return com.eseasky.common.code.utils.R
     * @description: ????????????????????????
     * @author: lc
     * @date: 2021/5/24 15:58
     * @params [orderSeatId]
     */
    @Override
    public R showShortDetail(String orderSeatId) {
        StaShortRepVO shortDetail = orderSeatListMapper.getShortDetail(orderSeatId);

        // ????????????????????????
        String showTime = StrUtil.subWithLength(shortDetail.getOrderStartTime(), 0, 16) + "~" +
                StrUtil.subWithLength(shortDetail.getOrderEndTime(), 0, 16);
        shortDetail.setShowTime(showTime);

        // ??????????????????
        Date userStartTime = shortDetail.getUseStartTime();
        Date userEndTime = shortDetail.getUseEndTime();
        if (ObjectUtil.isNotEmpty(userStartTime) && ObjectUtil.isNotEmpty(userEndTime)) {
            Double time = DateUtil.betweenMs(userStartTime, userEndTime) / (1000 * 3600.0);
            String format = new DecimalFormat("#.#").format(time) + "h";
            shortDetail.setUseTime(format);
        } else {
            shortDetail.setUseTime("0");
        }

        // ??????????????????,??????????????????????????????????????????
        Integer listState = shortDetail.getListState();
        String isLate = shortDetail.getIsLate();
        for (OrderConstant.ListStateEnum value : OrderConstant.ListStateEnum.values()) {
            if (listState.equals(value.getAftId())) {
                shortDetail.setRemark(value.getRemark()).setListState(value.getBefId());
            }
        }
        if (listState.equals(OrderConstant.listState.FINISH) && isLate.equals(1)) {
            shortDetail.setRemark("????????????");
        }
        return R.ok(shortDetail);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: ????????????????????????
     * @author: lc
     * @date: 2021/5/24 17:04
     * @params [orderSeatId]
     */
    @Override
    public R showLongDetail(String orderSeatId) {
        StaLongRepVO longDetail = orderSeatListMapper.getLongDetail(orderSeatId);

        // ????????????????????????
        String showTime = StrUtil.subWithLength(longDetail.getOrderStartTime(), 0, 10) + "~" +
                StrUtil.subWithLength(longDetail.getOrderEndTime(), 0, 10);
        longDetail.setShowTime(showTime);


        // ??????????????????,??????????????????????????????????????????
        Integer listState = longDetail.getListState();
        for (OrderConstant.ListStateEnum value : OrderConstant.ListStateEnum.values()) {
            if (listState.equals(value.getAftId())) {
                longDetail.setRemark(value.getRemark()).setListState(value.getBefId());
            }
        }

        // ??????????????????????????????
        String longRequireTimeStr = longDetail.getLongRequireTime();
        if (StrUtil.isEmpty(longRequireTimeStr)) {
            throw BusinessException.of("????????????");
        }
        String longRequireTime = parseHour(longRequireTimeStr) + "h";
        String useTime = parseHour(longDetail.getUseTime()) + "h";
        longDetail.setUseTime(useTime).setLongRequireTime(longRequireTime);

        // ??????????????????
        List<StaLongDetailRepVO> longRecords = orderSeatListMapper.getLongRecords(orderSeatId);

        longDetail.setRecords(longRecords);

        return R.ok(longDetail);
    }

    /**
     * @return
     * @description: ????????????????????????
     * @author: lc
     * @date: 2021/7/21 16:28
     * @params
     */
    @Override
    public R showGroupDetail(String orderGroupId) {

        StaGroupRepVO groupDetail = orderSeatListMapper.getGroupDetail(orderGroupId);

        // ????????????????????????
        String showTime = StrUtil.subWithLength(groupDetail.getOrderStartTime(), 0, 16) + "~" +
                StrUtil.subWithLength(groupDetail.getOrderEndTime(), 0, 16);
        groupDetail.setShowTime(showTime);

        // ????????????????????????
        LambdaQueryWrapper<OrderGroupDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
        detailQueryWrapper.select(OrderGroupDetail::getLearnTime
                , OrderGroupDetail::getUserName
                , OrderGroupDetail::getUserId
                , OrderGroupDetail::getUserState)
        .eq(OrderGroupDetail::getOrderGroupId,orderGroupId);
        List<OrderGroupDetail> orderGroupDetails = orderGroupDetailMapper.selectList(detailQueryWrapper);
        List<StaGroupUserRepVO> staGroupUserRepVOS = JSONObject.parseArray(JSON.toJSONString(orderGroupDetails), StaGroupUserRepVO.class);
        groupDetail.setUsers(staGroupUserRepVOS)
                    .setJoinCount(orderGroupDetails.size());

        // ???????????????
        int sum = staGroupUserRepVOS.stream().mapToInt(StaGroupUserRepVO::getLearnTime).sum();
        String useTime = parseHour(StrUtil.toString(sum))+"h";
        groupDetail.setUseTime(useTime).setRemark("??????");


        return R.ok(groupDetail);
    }


    @Override
    @Transactional
    public R getBuildAndRoomDropDown() {
        Set<String> orgIdList = spaceUtil.getOrgIdList();

        List<Map<String, String>> list = ((SpaceBuildMapper) buildService.getBaseMapper()).getBuildAndRoomStatisticsDropDown(orgIdList);
        if (CollectionUtils.isEmpty(list)) {
            R.ok(Lists.newArrayList());
        }
        LinkedListMultimap<String, DropDownVO> children = LinkedListMultimap.create();
        Map<String, String> parent = new LinkedHashMap<>();
        for (Map<String, String> map : list) {
            parent.putIfAbsent(map.get("buildId"), map.get("buildName"));
            children.put(map.get("buildId"), new DropDownVO(map.get("roomId"), map.get("roomName"), Lists.newArrayList()));
        }

        List<DropDownVO> result = new ArrayList<DropDownVO>();
        for (Map.Entry<String, String> entry : parent.entrySet()) {
            DropDownVO vo = new DropDownVO();
            vo.setValue(entry.getKey());
            vo.setLabel(entry.getValue());
            vo.setChildren(children.get(entry.getKey()));
            result.add(vo);
        }
        return R.ok(result);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: ????????????
     * @author: lc
     * @date: 2021/5/25 16:09
     * @params [orderListReqVO]
     */
    @Override
    public void outOrderList(OrderListReqVO orderListReqVO, HttpServletResponse httpServletResponse) {
        // ??????????????????????????????
        orderListReqVO.setPageSize(-1);
        Page<OrderListRepVO> page = (Page) showOrderList(orderListReqVO).getData();
        List<OrderListRepVO> records = page.getRecords();

        // ???excel????????????xls
        ExportParams exportParams = new ExportParams();
        exportParams.setType(ExcelType.XSSF);
        List<ListExcelDTO> listExcelDTOS = JSONObject.parseArray(JSON.toJSONString(records), ListExcelDTO.class);
        // String format = DatePattern.NORM_DATE_FORMAT.format(new Date());
        try {
            ExcelUtils.exportExcel(listExcelDTOS, ListExcelDTO.class, "????????????", exportParams, httpServletResponse);
        } catch (IOException e) {
            log.info("????????????");
            e.printStackTrace();
        }
        return;
    }


    /**
     * @return java.lang.String
     * @description: ???????????????(????????????????????????)
     * @author: lc
     * @date: 2021/4/22 10:11
     * @params [sec]
     */
    public String parseHour(String sec) {
        int i = Integer.parseInt(sec);
        String format = new DecimalFormat("#.#").format(i / 3600.0);
        return format;
    }


}
