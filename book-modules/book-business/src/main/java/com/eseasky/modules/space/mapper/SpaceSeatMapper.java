package com.eseasky.modules.space.mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.vo.request.SaveSeatVO;
import com.eseasky.modules.space.vo.response.SeatInfoForQuickOrder;
import com.eseasky.modules.space.vo.response.SeatInfoToOrder;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Repository
public interface SpaceSeatMapper extends BaseMapper<SpaceSeat> {

    List<SaveSeatVO> findListByRoomId(@Param("roomId") String roomId);

    /** 统计中心座位：根据空间id，判断这些座位在时间点是否被占用 */
    List<JSONObject> getInUsedSeatForStatistic( //
            @Param("startDate") Date startDate, //
            @Param("roomId") String roomId);

    /** 统计中心房间：根据空间ids，判断这些座位在时间点是否被占用 */
    List<JSONObject> getOrderedSeatByRoomIds( //
            @Param("startDate") Date startDate, //
            @Param("roomIdList") Collection<String> roomIdList);

    /** 统计中心房间：根据空间ids，判断这些空间在时间点是否被占用 */
    List<String> getMeetingRoomInUsedList( //
            @Param("startDate") Date startDate, //
            @Param("roomIdList") Collection<String> roomIdList);


    /** 为订单获取座位信息，非删除，非禁用 */
    SeatInfoToOrder getSeatInfoForOrder(@Param("seatId") String seatId);

    /** 为订单获取座位信息，非删除 */
    SeatInfoToOrder getGroupInfoForOrder(@Param("groupId") String groupId);

    SeatInfoForQuickOrder getSeatInfoForQuickOrder(@Param("seatId") String seatId);

    /** 快速预约，获取一个座位id */
    String getSeatIdForQuickOrder( //
            @Param("freeList") List<String> freeList, //
            @Param("buildId") String buildId, //
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate);


    /******************/
    /** 单人长租和短租，只用于综合楼。可预约confId、可见seat、非删除seat，并且是分页buildId，排除时间段内占用的seatId */
    List<JSONObject> singleOrderForBuild( //
            @Param("freeList") Collection<String> freeList, // 可预约confId
            @Param("buildIdList") Collection<String> buildIdList, // 分页buildId
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate, //
            @Param("orderType") Integer orderType);

    /** 会议室长租和短租，只用于综合楼。可预约confId、可见seat、非删除seat，并且是分页buildId，排除时间段内占用的roomId */
    List<JSONObject> multiOrderForBuild( //
            @Param("freeList") Collection<String> freeList, // 可预约confId
            @Param("buildIdList") Collection<String> buildIdList, // 分页buildId
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate, //
            @Param("orderType") Integer orderType);


    /******************/
    /** 单人长租和短租，只用于房间。可预约confId、可见seat、非删除seat、指定buildId，排除时间段内占用的seatId */
    List<JSONObject> singleOrderForRoom( //
            @Param("freeList") Collection<String> freeList, // 可预约confId
            @Param("buildId") String buildId, // 指定buildId
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate, //
            @Param("orderType") Integer orderType);

    /** 会议室长租和短租，只用于房间。可预约confId、可见seat、非删除seat、指定buildId，排除时间段内占用的roomId */
    List<JSONObject> multiOrderForRoom( //
            @Param("freeList") Collection<String> freeList, // 可预约confId
            @Param("buildId") String buildId, // 指定buildId
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate, //
            @Param("orderType") Integer orderType);

    /******************/
    /** 单人长租和短租，只用于座位。可预约confId、可见seat、非删除seat、指定roomId，排除时间段内占用的seatId */
    List<String> singleOrderForSeat( //
            @Param("freeList") Collection<String> freeList, // 可预约confId
            @Param("roomId") String roomId, // 指定roomId
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate);

    /** 单人短租拼团，根据roomId查询可用groupId，排除时间段内占用的groupId */
    List<SpaceSeat> groupOrderForSeat( //
            @Param("freeList") Collection<String> freeList, // 可预约confId
            @Param("roomId") String roomId, //
            @Param("startDate") Date startDate, //
            @Param("endDate") Date endDate);

}
