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
     * @description: 查看空间统计
     * @author: lc
     * @date: 2021/5/20 17:42
     * @params []
     */
    public R showSpaceStatistics(StaSpcReqVO staSpcReqVO) {

        // 获取传参信息
        Integer orderType = staSpcReqVO.getOrderType();

        // 预约类型为短租，获取短租统计信息
        if (OrderConstant.OrderType.SHORT_RENT.equals(orderType)) {
            Page shortStatics = getShortStatics(staSpcReqVO);
            return R.ok(shortStatics);
        }

        // 预约类型为长租，获取单人长租统计信息
        if (OrderConstant.OrderType.LONG_RENT.equals(orderType)) {
            Page longStatics = getLongStatics(staSpcReqVO);
            return R.ok(longStatics);
        }

        // 预约类型为拼团，获取拼团统计信息
        if (OrderConstant.OrderType.GROUP_RENT.equals(orderType)) {
            Page shortStatics = getGroupStatics(staSpcReqVO);
            return R.ok(shortStatics);
        }

        // 预约类型为会议室短租，获取会议室短租统计信息
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            Page longStatics = getMeetShortStatics(staSpcReqVO);
            return R.ok(longStatics);
        }

        // 预约类型为会议室
        else {
            Page longStatics = getMeetLongStatics(staSpcReqVO);
            return R.ok(longStatics);
        }

    }

    /**
     * @return
     * @description: 获取单人短租（拼团）统计信息
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getShortStatics(StaSpcReqVO staSpcReqVO) {
        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());
        // 计算所给时间覆盖天数
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // 获取短租统计信息
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceShortSta(page, staSpcReqVO);

        // 计算平均数据
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            String aveCount = decimalFormat.format(spaceStatistic.getTotalCount() / days);
            String totTime = parseHour(spaceStatistic.getTotalTime());
            String aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // 获取分页数据信息
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: 获取单人长租短统计信息
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getLongStatics(StaSpcReqVO staSpcReqVO) {

        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());

        // 获取空间统计信息
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceLongSta(page, staSpcReqVO);

        // 计算所给时间覆盖天数
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // 计算平均信息
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            Integer totalCount = spaceStatistic.getTotalCount();
            String totalTime = spaceStatistic.getTotalTime();
            // 若总次数不为0，计算平均次数
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

        // 获取分页数据信息
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: 获取单人短租统计信息
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getGroupStatics(StaSpcReqVO staSpcReqVO) {
        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());
        // 计算所给时间覆盖天数
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // 获取短租统计信息
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceGroupSta(page, staSpcReqVO);

        // 计算平均数据
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            String aveCount = decimalFormat.format(spaceStatistic.getTotalCount() / days);
            String totTime = parseHour(spaceStatistic.getTotalTime());
            String aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // 获取分页数据信息
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: 获取单人长租短统计信息
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getMeetShortStatics(StaSpcReqVO staSpcReqVO) {

        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());

        // 获取空间统计信息
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceMeetShortSta(page, staSpcReqVO);

        // 计算所给时间覆盖天数
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // 计算平均信息
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            String aveCount = decimalFormat.format(spaceStatistic.getTotalCount() / days);
            String totTime = parseHour(spaceStatistic.getTotalTime());
            String aveTime = decimalFormat.format(Convert.toDouble(totTime) / days);
            spaceStatistic.setTotalTime(totTime).setAverageCount(aveCount).setAverageTime(aveTime);
        }

        // 获取分页数据信息
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return
     * @description: 获取单人长租短统计信息
     * @author: lc
     * @date: 2021/7/19 14:03
     * @params
     */
    public Page getMeetLongStatics(StaSpcReqVO staSpcReqVO) {

        Page<SpaceStatisticsRepVO> page = new Page<>(staSpcReqVO.getPageNum(), staSpcReqVO.getPageSize());

        // 获取空间统计信息
        ArrayList<SpaceStatisticsRepVO> spaceStatistics = orderSeatListMapper.getSpaceMeetLongSta(page, staSpcReqVO);

        // 计算所给时间覆盖天数
        Double days = Convert.toDouble(DateUtil.betweenDay(staSpcReqVO.getStartDate(), staSpcReqVO.getEndDate(), false) + 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // 计算平均信息
        for (SpaceStatisticsRepVO spaceStatistic : spaceStatistics) {
            Integer totalCount = spaceStatistic.getTotalCount();
            String totalTime = spaceStatistic.getTotalTime();
            // 若总次数不为0，计算平均次数
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

        // 获取分页数据信息
        return page.setRecords(spaceStatistics);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 查看空间分析
     * @author: lc
     * @date: 2021/5/24 16:28
     * @params [roomId]
     */
    public R showSpaceAnalysis(AnaRoomReqVO anaRoomReqVO) {

        // 获取传参信息
        Integer orderType = anaRoomReqVO.getOrderType();

        // 获取分析数据
        ArrayList<SpaceAnalysisRepVO> spaceAnalysis = null;
        // 单人短租分析数据
        if (OrderConstant.OrderType.SHORT_RENT.equals(orderType)) {
            spaceAnalysis = orderSeatListMapper.getShortAnalysis(anaRoomReqVO);
        }
        // 拼团分析数据
        if (OrderConstant.OrderType.GROUP_RENT.equals(orderType)) {
            spaceAnalysis = orderSeatListMapper.getGroupAnalysis(anaRoomReqVO);
        }
        // 单人短租分析数据
        if (OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING.equals(orderType)) {
            spaceAnalysis = orderSeatListMapper.getMeetingAnalysis(anaRoomReqVO);
        }

        return R.ok(spaceAnalysis);
    }

    /**
     * @return R
     * @description: 查看人员统计
     * @author: lc
     * @date: 2021/5/20 17:42
     * @params []
     */
    @Override
    public R showUserStatistics(StaUserReqVO staUserReqVO) {

        Page<UserStatisticsRepVO> page = new Page<>(staUserReqVO.getPageNum(), staUserReqVO.getPageSize());

        DecimalFormat decimalFormat = new DecimalFormat("#.#");

        // 获取用户统计信息
        ArrayList<UserStatisticsRepVO> userStatistics = orderSeatListMapper.getUserStatistics(page, staUserReqVO);
        for (UserStatisticsRepVO userStatistic : userStatistics) {
            String orderCount = userStatistic.getOrderCount();
            String zero = "0";
            // 若预约次数不为0，则计算平均时间
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
     * @description: 查看人员分析
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
     * @description: 查看订单列表
     * @author: lc
     * @date: 2021/4/16 16:22
     * @params [OrderListReqVO]
     */
    @Override
    public R showOrderList(OrderListReqVO orderListReqVO) {

        //获取传参信息
        Integer orderType = orderListReqVO.getOrderType();

        // 按预约类型分别分页查询订单信息
        Page<OrderListRepVO> page = new Page<>(orderListReqVO.getPageNum(), orderListReqVO.getPageSize());
        List<OrderListRepVO> lists = Lists.newArrayList();
        // 若类型为单人预约，查看单人预约详情
        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT) || orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            lists = orderSeatListMapper.getBuildSingleList(page, orderListReqVO);
        }
        // 若类型为拼团预约，查看拼团预约详情
        if (orderType.equals(OrderConstant.OrderType.GROUP_RENT)) {
            lists = orderSeatListMapper.getBuildGroupList(page, orderListReqVO);
        }
        // 若类型为会议预约，查看会议预约详情
        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING) || orderType.equals(OrderConstant.OrderType.LONG_RENT_ORDER_MEETING)) {
            lists = orderSeatListMapper.getBuildMeetingList(page, orderListReqVO);
        }

        // 处理前端展示地点
        for (OrderListRepVO list : lists) {
            // 拼接前端展示地点
            list.setArea(list.getBuildName() + list.getFloorNum() + "F" + list.getRoomName() + list.getRoomNum());
        }

        // 处理前端展示时间
        // 短租
        if (shortRent.contains(orderType)) {
            for (OrderListRepVO list : lists) {
                String showTime = StrUtil.subWithLength(list.getOrderStartTime(), 0, 16) + "~" +
                        StrUtil.subWithLength(list.getOrderEndTime(), 0, 16);
                list.setShowTime(showTime);
            }
        }
        // 长租
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
     * @description: 查看短租订单详情
     * @author: lc
     * @date: 2021/5/24 15:58
     * @params [orderSeatId]
     */
    @Override
    public R showShortDetail(String orderSeatId) {
        StaShortRepVO shortDetail = orderSeatListMapper.getShortDetail(orderSeatId);

        // 拼接前端展示时间
        String showTime = StrUtil.subWithLength(shortDetail.getOrderStartTime(), 0, 16) + "~" +
                StrUtil.subWithLength(shortDetail.getOrderEndTime(), 0, 16);
        shortDetail.setShowTime(showTime);

        // 计算使用时长
        Date userStartTime = shortDetail.getUseStartTime();
        Date userEndTime = shortDetail.getUseEndTime();
        if (ObjectUtil.isNotEmpty(userStartTime) && ObjectUtil.isNotEmpty(userEndTime)) {
            Double time = DateUtil.betweenMs(userStartTime, userEndTime) / (1000 * 3600.0);
            String format = new DecimalFormat("#.#").format(time) + "h";
            shortDetail.setUseTime(format);
        } else {
            shortDetail.setUseTime("0");
        }

        // 插入违约详情,后端展示状态改为前端展示状态
        Integer listState = shortDetail.getListState();
        String isLate = shortDetail.getIsLate();
        for (OrderConstant.ListStateEnum value : OrderConstant.ListStateEnum.values()) {
            if (listState.equals(value.getAftId())) {
                shortDetail.setRemark(value.getRemark()).setListState(value.getBefId());
            }
        }
        if (listState.equals(OrderConstant.listState.FINISH) && isLate.equals(1)) {
            shortDetail.setRemark("迟到打卡");
        }
        return R.ok(shortDetail);
    }

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 查看长租订单详情
     * @author: lc
     * @date: 2021/5/24 17:04
     * @params [orderSeatId]
     */
    @Override
    public R showLongDetail(String orderSeatId) {
        StaLongRepVO longDetail = orderSeatListMapper.getLongDetail(orderSeatId);

        // 拼接前端展示时间
        String showTime = StrUtil.subWithLength(longDetail.getOrderStartTime(), 0, 10) + "~" +
                StrUtil.subWithLength(longDetail.getOrderEndTime(), 0, 10);
        longDetail.setShowTime(showTime);


        // 插入违约详情,后端展示状态改为前端展示状态
        Integer listState = longDetail.getListState();
        for (OrderConstant.ListStateEnum value : OrderConstant.ListStateEnum.values()) {
            if (listState.equals(value.getAftId())) {
                longDetail.setRemark(value.getRemark()).setListState(value.getBefId());
            }
        }

        // 计算阈值和已使用时长
        String longRequireTimeStr = longDetail.getLongRequireTime();
        if (StrUtil.isEmpty(longRequireTimeStr)) {
            throw BusinessException.of("阈值为空");
        }
        String longRequireTime = parseHour(longRequireTimeStr) + "h";
        String useTime = parseHour(longDetail.getUseTime()) + "h";
        longDetail.setUseTime(useTime).setLongRequireTime(longRequireTime);

        // 查看签到记录
        List<StaLongDetailRepVO> longRecords = orderSeatListMapper.getLongRecords(orderSeatId);

        longDetail.setRecords(longRecords);

        return R.ok(longDetail);
    }

    /**
     * @return
     * @description: 查看拼团订单详情
     * @author: lc
     * @date: 2021/7/21 16:28
     * @params
     */
    @Override
    public R showGroupDetail(String orderGroupId) {

        StaGroupRepVO groupDetail = orderSeatListMapper.getGroupDetail(orderGroupId);

        // 拼接前端展示时间
        String showTime = StrUtil.subWithLength(groupDetail.getOrderStartTime(), 0, 16) + "~" +
                StrUtil.subWithLength(groupDetail.getOrderEndTime(), 0, 16);
        groupDetail.setShowTime(showTime);

        // 获取拼团人员信息
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

        // 计算总时长
        int sum = staGroupUserRepVOS.stream().mapToInt(StaGroupUserRepVO::getLearnTime).sum();
        String useTime = parseHour(StrUtil.toString(sum))+"h";
        groupDetail.setUseTime(useTime).setRemark("暂无");


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
     * @description: 导出列表
     * @author: lc
     * @date: 2021/5/25 16:09
     * @params [orderListReqVO]
     */
    @Override
    public void outOrderList(OrderListReqVO orderListReqVO, HttpServletResponse httpServletResponse) {
        // 根据条件查询全部数据
        orderListReqVO.setPageSize(-1);
        Page<OrderListRepVO> page = (Page) showOrderList(orderListReqVO).getData();
        List<OrderListRepVO> records = page.getRecords();

        // 将excel格式改为xls
        ExportParams exportParams = new ExportParams();
        exportParams.setType(ExcelType.XSSF);
        List<ListExcelDTO> listExcelDTOS = JSONObject.parseArray(JSON.toJSONString(records), ListExcelDTO.class);
        // String format = DatePattern.NORM_DATE_FORMAT.format(new Date());
        try {
            ExcelUtils.exportExcel(listExcelDTOS, ListExcelDTO.class, "预约列表", exportParams, httpServletResponse);
        } catch (IOException e) {
            log.info("导出异常");
            e.printStackTrace();
        }
        return;
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
        return format;
    }


}
