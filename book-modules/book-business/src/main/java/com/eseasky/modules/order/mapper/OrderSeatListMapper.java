package com.eseasky.modules.order.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.modules.order.dto.GetRepeatCountDTO;
import com.eseasky.modules.order.dto.OrderListInfoDTO;
import com.eseasky.modules.order.dto.OrderSeatListDTO;
import com.eseasky.modules.order.dto.StatisticsLearnTimeDTO;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.vo.request.*;
import com.eseasky.modules.order.vo.response.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-12
 */
@Repository
public interface OrderSeatListMapper extends BaseMapper<OrderSeatList> {

    /** 查找某时间段某一区域被占座位*/
    List<Map<String,Object>>  getUsedSeat(OrderListInfoDTO orderListInfoDTO);

    /** 统计模块 按条件查询单人预约列表*/
    List<OrderListRepVO> getBuildSingleList(Page page, @Param("VO") OrderListReqVO orderListReqVO);

    /** 统计模块 按条件查询拼团预约列表*/
    List<OrderListRepVO> getBuildGroupList(Page page, @Param("VO") OrderListReqVO orderListReqVO);

    /** 统计模块 按条件查询会议预约列表*/
    List<OrderListRepVO> getBuildMeetingList(Page page, @Param("VO") OrderListReqVO orderListReqVO);


    /** */
    List<StatisticsLearnTimeDTO> getStatisticsDate(@Param("month") String month,@Param("type") Integer statisticsType );

    List<OftenUserAreaRepVO> getOftenUseArea(String userId);

    /** 查看最近一次短租预约订单*/
    FirstPageRePVO  getShortLastList(String userId);

    /** 查看最近一次长租预约订单*/
    FirstPageRePVO getLongLastList(String usrId);

    /** 查看违约次数*/
    Integer getViolateCount(@Param("userId")String userId,@Param("month")String month);

    /** 查看违约次数*/
    Integer getInBLCount(@Param("userId")String userId,@Param("month")String month);

    /** 查看订单详情*/
    OrderListDetailVO showListDetail(String orderSeatId);

    /** 查看用户短租订单*/
    List<UserRentListVO>  getUserShortRent(Page page,@Param("vo") UserListReqVO userListReqVO);

    /** 查看用户长租订单*/
    List<UserRentListVO>  getUserLongRent(Page page,@Param("vo") UserListReqVO userListReqVO);

    /** 获取打卡界面信息*/
    ShowClockRepVO getClockInfo(ShowClockReqVO showClockReqVO);

    /** 手机端，座位预约界面，根据时间段查询座位的预约状态 */
    List<JSONObject> getOrderedSeatByRoomId(Map<String, Object> map);

    /** 查看自己订单是否有冲突*/
    Integer getRepeatCount(GetRepeatCountDTO getRepeatCountDTO);

    /** 查看该座位是否已被占用*/
    Integer getSeatRepeat(OrderSeatList orderSeatList);

    /** 统计短租每天的学习时间*/
    List<StatisticsLearnTimeDTO> getShortDayLearnTime(@Param("month") String month,@Param("userId") String userId);

    /** 统计短租每天的学习时间*/
    List<StatisticsLearnTimeDTO> getLongDayLearnTime(@Param("month") String month,@Param("userId") String userId);

    /** 统计拼团每天的学习时间*/
    List<StatisticsLearnTimeDTO> getGroupDayLearnTime(@Param("month") String month,@Param("userId") String userId);

    /** 查询当月预约次数*/
    Integer  getMonthCount(@Param("month") String month,@Param("userId") String userId);

    /** 获取学生学习时间排行*/
    List<StatisticsLearnTimeDTO> getStudentTop(String month);

    List<StatisticsLearnTimeDTO> getSpaceTop(String month);

    /** 获取空间统计信息 (短租)*/
    ArrayList<SpaceStatisticsRepVO> getSpaceShortSta(Page page, @Param("vo") StaSpcReqVO staSpcReqVO);

    /** 获取空间统计信息 （长租）*/
    ArrayList<SpaceStatisticsRepVO> getSpaceLongSta(Page page, @Param("vo") StaSpcReqVO staSpcReqVO);

    /** 获取空间统计信息 (拼团)*/
    ArrayList<SpaceStatisticsRepVO> getSpaceGroupSta(Page page, @Param("vo") StaSpcReqVO staSpcReqVO);

    /** 获取空间统计信息 （会议短租）*/
    ArrayList<SpaceStatisticsRepVO> getSpaceMeetShortSta(Page page, @Param("vo") StaSpcReqVO staSpcReqVO);

    /** 获取空间统计信息 （会议短租）*/
    ArrayList<SpaceStatisticsRepVO> getSpaceMeetLongSta(Page page, @Param("vo") StaSpcReqVO staSpcReqVO);

    /** 获取单人短租空间分析数据*/
    ArrayList<SpaceAnalysisRepVO> getShortAnalysis(@Param("vo") AnaRoomReqVO anaRoomReqVO);

    /** 获取拼团空间分析数据*/
    ArrayList<SpaceAnalysisRepVO> getGroupAnalysis(@Param("vo") AnaRoomReqVO anaRoomReqVO);

    /** 获取会议短租空间分析数据*/
    ArrayList<SpaceAnalysisRepVO> getMeetingAnalysis(@Param("vo") AnaRoomReqVO anaRoomReqVO);

    /** 获取用户统计信息*/
    ArrayList<UserStatisticsRepVO> getUserStatistics(Page page, @Param("vo") StaUserReqVO staUserReqVO);

    /** 获取用户分析数据*/
    ArrayList<UserAnalysisRepVO> getUserAnalysis(String userId);

    /** 短租订单详情*/
    StaShortRepVO  getShortDetail(String orderSeatId);

    /** 长租订单详情*/
    StaLongRepVO getLongDetail(String orderSeatId);

    /** 拼团订单详情*/
    StaGroupRepVO getGroupDetail(String orderGroupId);



    /** 长租签到记录*/
    List<StaLongDetailRepVO> getLongRecords(String orderSeatId);

    /** 获取订单详情*/
    OrderSeatListDTO getOrderSeatListDetail(String orderSeatId);
}
