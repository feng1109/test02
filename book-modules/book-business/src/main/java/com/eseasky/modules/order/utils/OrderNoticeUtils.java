package com.eseasky.modules.order.utils;

import com.alibaba.fastjson.JSONObject;
import com.eseasky.modules.order.dto.OrderNoticeDTO;
import com.google.common.collect.Lists;

import cn.hutool.core.util.IdUtil;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.SendNoticeDTO;
import com.eseasky.modules.order.entity.OrderNotice;
import com.eseasky.modules.order.vo.response.OrderNoticeRepVO;

import java.util.Date;
import java.util.List;

/**
 * 通知工具类
 *
 * @author wpt
 */
public class OrderNoticeUtils {

    public static OrderNotice getOrderNotice(String userId, Integer noticeType, Integer titleType, Integer contentType, String title, String content) {
        //添加小程序通知
        OrderNotice orderNotice = new OrderNotice();
        orderNotice.setOrderNoticeId(IdUtil.simpleUUID());
        //会议室小程序通知打开需要给参会人添加，
        orderNotice.setUserId(userId);
        orderNotice.setNoticeType(noticeType);
        orderNotice.setTitleType(titleType);
        orderNotice.setContentType(contentType);
        orderNotice.setTitle(title);
        orderNotice.setContent(content);
        orderNotice.setSendTime(new Date());
        orderNotice.setIsRead(OrderConstant.Switch.NO);
        orderNotice.setDelFlag("1");
        return orderNotice;
    }

    /**
     * 获取发送
     *
     * @param userId
     * @param noticeType
     * @param content
     * @return
     */
    public static SendNoticeDTO getSendNoticeDTO(String userId, Integer noticeType, Integer titleType, Integer contentType, String title, String content) {
        SendNoticeDTO sendNoticeDTO = new SendNoticeDTO();
        sendNoticeDTO.setUserId(userId);
        sendNoticeDTO.setNoticeType(noticeType);
        sendNoticeDTO.setTitleType(titleType);
        sendNoticeDTO.setContentType(contentType);
        sendNoticeDTO.setTitle(title);
        sendNoticeDTO.setContent(content);
        sendNoticeDTO.setSendTime(new Date());
        return sendNoticeDTO;
    }

    /**
     * 从 orderNotice 集合 获取 orderNoticeRepVO集合
     *
     * @param records
     * @return
     */
    public static List<OrderNoticeRepVO> getOrderNoticeRepVOListByOrderNoticeList(List<OrderNotice> records) {
        List<OrderNoticeRepVO> orderNoticeRepVOList = Lists.newArrayList();
        for (OrderNotice orderNotice : records) {
            orderNoticeRepVOList.add(getOrderNoticeRepVOByOrderNotice(orderNotice));
        }
        return orderNoticeRepVOList;
    }

    /**
     * 从orderNotice 获取 orderNoticeRepVo
     *
     * @param orderNotice
     * @return
     */
    public static OrderNoticeRepVO getOrderNoticeRepVOByOrderNotice(OrderNotice orderNotice) {
        OrderNoticeRepVO orderNoticeRepVO = new OrderNoticeRepVO();
        orderNoticeRepVO.setOrderNoticeId(orderNotice.getOrderNoticeId());
        orderNoticeRepVO.setUserId(orderNotice.getUserId());
        orderNoticeRepVO.setNoticeType(orderNotice.getNoticeType());
        orderNoticeRepVO.setContentType(orderNotice.getContentType());
        orderNoticeRepVO.setTitle(orderNotice.getTitle());
        orderNoticeRepVO.setContent(JSONObject.parseObject(orderNotice.getContent(), OrderNoticeDTO.class));
        orderNoticeRepVO.setIsRead(orderNotice.getIsRead());
        orderNoticeRepVO.setSendTime(orderNotice.getSendTime());
        return orderNoticeRepVO;
    }
}
