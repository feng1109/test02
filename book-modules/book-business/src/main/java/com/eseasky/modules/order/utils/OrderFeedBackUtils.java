package com.eseasky.modules.order.utils;

import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.OrderGroupListDetailedDTO;
import com.eseasky.modules.order.dto.OrderSeatListDTO;
import com.eseasky.modules.order.entity.OrderFeedBack;
import com.eseasky.modules.order.vo.request.OrderFeedBackAddReqVO;
import com.eseasky.modules.order.vo.response.FeedBackRepVO;
import com.eseasky.modules.order.vo.response.OrderMeetingListDetailRepVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户反馈工具类
 */
public class OrderFeedBackUtils {

    /**
     * 从vo 获取 entity
     *
     * @param orderFeedBackAddReqVO
     * @param orderDetail
     * @param userId
     * @return
     */
    public static OrderFeedBack getOrderFreeBackByFeedBackAddReqVOAndMeeting(OrderFeedBackAddReqVO orderFeedBackAddReqVO, OrderMeetingListDetailRepVO orderDetail, String userId) {
        OrderFeedBack orderFeedBack = new OrderFeedBack();
        orderFeedBack.setFeedBackId(null);
        orderFeedBack.setBuildId(orderDetail.getBuildId());
        orderFeedBack.setBuildName(orderDetail.getBuildName());
        orderFeedBack.setBuildNum(orderDetail.getBuildNum());
        orderFeedBack.setFloorId(orderDetail.getFloorId());
        orderFeedBack.setFloorNum(String.valueOf(orderDetail.getFloorNum()));
        orderFeedBack.setFloorName(orderDetail.getFloorName());
        orderFeedBack.setRoomId(orderDetail.getRoomId());
        orderFeedBack.setRoomName(orderDetail.getRoomName());
        orderFeedBack.setRoomNum(orderDetail.getRoomNum());
        orderFeedBack.setUserId(orderDetail.getUserId());
        orderFeedBack.setUserName(orderDetail.getUserName());
        orderFeedBack.setType(orderFeedBackAddReqVO.getType());
        orderFeedBack.setState(OrderConstant.OrderFeedBackState.NO);
        orderFeedBack.setContent(String.valueOf(orderFeedBackAddReqVO.getContent()));
        orderFeedBack.setCreateUserId(userId);
        orderFeedBack.setCreateTime(new Date());
        orderFeedBack.setOrderListId(orderFeedBackAddReqVO.getOrderListId());
        orderFeedBack.setOrderType(orderDetail.getOrderType());
        orderFeedBack.setUpdateUserId(userId);
        orderFeedBack.setUpdateTime(new Date());
        orderFeedBack.setDelFlag("0");
        return orderFeedBack;
    }

    public static OrderFeedBack getOrderFreeBackByFeedBackAddReqVOAndSeat(OrderFeedBackAddReqVO orderFeedBackAddReqVO, OrderSeatListDTO orderSeatListDetail, String userId) {
        OrderFeedBack orderFeedBack = new OrderFeedBack();
        orderFeedBack.setFeedBackId(null);
        orderFeedBack.setBuildId(orderSeatListDetail.getBuildId());
        orderFeedBack.setBuildName(orderSeatListDetail.getBuildName());
        orderFeedBack.setBuildNum(orderSeatListDetail.getBuildNum());
        orderFeedBack.setFloorId(orderSeatListDetail.getFloorId());
        orderFeedBack.setFloorNum(String.valueOf(orderSeatListDetail.getFloorNum()));
        orderFeedBack.setFloorName(orderSeatListDetail.getFloorName());
        orderFeedBack.setRoomId(orderSeatListDetail.getRoomId());
        orderFeedBack.setRoomName(orderSeatListDetail.getRoomName());
        orderFeedBack.setRoomNum(orderSeatListDetail.getRoomNum());
        orderFeedBack.setUserId(orderSeatListDetail.getUserId());
        orderFeedBack.setUserName(orderSeatListDetail.getUserName());
        orderFeedBack.setType(orderFeedBackAddReqVO.getType());
        orderFeedBack.setState(OrderConstant.OrderFeedBackState.NO);
        orderFeedBack.setContent(String.valueOf(orderFeedBackAddReqVO.getContent()));
        orderFeedBack.setOrderListId(orderFeedBackAddReqVO.getOrderListId());
        orderFeedBack.setOrderType(orderSeatListDetail.getOrderType());
        orderFeedBack.setCreateUserId(userId);
        orderFeedBack.setCreateTime(new Date());
        orderFeedBack.setUpdateUserId(userId);
        orderFeedBack.setUpdateTime(new Date());
        orderFeedBack.setDelFlag("0");
        return orderFeedBack;

    }

    /**
     * 从entity集合 获取 vo集合
     *
     * @param records
     * @return
     */
    public static List<FeedBackRepVO> getFeedBackRepVOListByFeedBackList(List<OrderFeedBack> records) {
        ArrayList<FeedBackRepVO> feedBackRepVOList = new ArrayList<>();
        for (OrderFeedBack record : records) {
            feedBackRepVOList.add(getFeedBackRepVOByFeedBack(record));
        }
        return feedBackRepVOList;
    }

    /**
     * 从entity 获取 vo
     *
     * @param orderFeedBack
     * @return
     */
    public static FeedBackRepVO getFeedBackRepVOByFeedBack(OrderFeedBack orderFeedBack) {
        FeedBackRepVO feedBackRepVO = new FeedBackRepVO();
        feedBackRepVO.setFeedBackId(orderFeedBack.getFeedBackId());
        feedBackRepVO.setBuildId(orderFeedBack.getBuildId());
        feedBackRepVO.setBuildName(orderFeedBack.getBuildName());
        feedBackRepVO.setBuildNum(orderFeedBack.getBuildNum());
        feedBackRepVO.setFloorId(orderFeedBack.getFloorId());
        feedBackRepVO.setFloorNum(orderFeedBack.getFloorNum());
        feedBackRepVO.setFloorName(orderFeedBack.getFloorName());
        feedBackRepVO.setRoomId(orderFeedBack.getRoomId());
        feedBackRepVO.setRoomName(orderFeedBack.getRoomName());
        feedBackRepVO.setRoomNum(orderFeedBack.getRoomNum());
        feedBackRepVO.setUserId(orderFeedBack.getUserId());
        feedBackRepVO.setUserName(orderFeedBack.getUserName());
        feedBackRepVO.setType(orderFeedBack.getType());
        feedBackRepVO.setState(orderFeedBack.getState());
        feedBackRepVO.setContent(orderFeedBack.getContent());
        feedBackRepVO.setCreateUserId(orderFeedBack.getCreateUserId());
        feedBackRepVO.setCreateTime(orderFeedBack.getCreateTime());
        feedBackRepVO.setOrderListId(orderFeedBack.getOrderListId());
        feedBackRepVO.setUpdateUserId(orderFeedBack.getUpdateUserId());
        feedBackRepVO.setUpdateTime(orderFeedBack.getUpdateTime());
        feedBackRepVO.setDelFlag(orderFeedBack.getDelFlag());
        return feedBackRepVO;
    }

    /**
     * 从拼团详情中获取反馈数据
     * @param orderFeedBackAddReqVO
     * @param orderGroupListDetailedDTO
     * @param userId
     * @return
     */
    public static OrderFeedBack getOrderFreeBackByFeedBackAddReqVOAndGroup(OrderFeedBackAddReqVO orderFeedBackAddReqVO, OrderGroupListDetailedDTO orderGroupListDetailedDTO,String userId) {
        OrderFeedBack orderFeedBack = new OrderFeedBack();
        orderFeedBack.setFeedBackId(null);
        orderFeedBack.setOrderListId(orderFeedBackAddReqVO.getOrderListId());
        orderFeedBack.setOrderType(orderGroupListDetailedDTO.getOrderType());
        orderFeedBack.setBuildId(orderGroupListDetailedDTO.getBuildId());
        orderFeedBack.setBuildName(orderGroupListDetailedDTO.getBuildName());
        orderFeedBack.setBuildNum(orderGroupListDetailedDTO.getBuildNum());
        orderFeedBack.setFloorId(orderGroupListDetailedDTO.getFloorId());
        orderFeedBack.setFloorNum(String.valueOf(orderGroupListDetailedDTO.getFloorNum()));
        orderFeedBack.setFloorName(orderGroupListDetailedDTO.getFloorName());
        orderFeedBack.setRoomId(orderGroupListDetailedDTO.getRoomId());
        orderFeedBack.setRoomName(orderGroupListDetailedDTO.getRoomName());
        orderFeedBack.setRoomNum(orderGroupListDetailedDTO.getRoomNum());
        orderFeedBack.setUserId(orderGroupListDetailedDTO.getUserId());
        orderFeedBack.setUserName(orderGroupListDetailedDTO.getUserName());
        orderFeedBack.setType(orderFeedBackAddReqVO.getType());
        orderFeedBack.setState(orderGroupListDetailedDTO.getListState());
        orderFeedBack.setContent(orderFeedBackAddReqVO.getContent());
        orderFeedBack.setCreateUserId(userId);
        orderFeedBack.setCreateTime(orderGroupListDetailedDTO.getCreateTime());
        orderFeedBack.setUpdateUserId(userId);
        orderFeedBack.setUpdateTime(orderGroupListDetailedDTO.getUpdateTime());
        orderFeedBack.setDelFlag("0");
        return orderFeedBack;
    }
}
