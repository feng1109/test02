package com.eseasky.modules.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.book.redis.starter.template.RedisRepository;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.*;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.dto.OrderGroupListDetailedDTO;
import com.eseasky.modules.order.dto.OrderSeatListDTO;
import com.eseasky.modules.order.entity.OrderFeedBack;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.entity.OrderGroupList;
import com.eseasky.modules.order.mapper.*;
import com.eseasky.modules.order.service.OrderFeedBackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.modules.order.utils.OrderFeedBackUtils;
import com.eseasky.modules.order.vo.request.OrderFeedBackAddReqVO;
import com.eseasky.modules.order.vo.request.OrderFeedBackHandReqVO;
import com.eseasky.modules.order.vo.request.OrderFeedBackPageReqVO;
import com.eseasky.modules.order.vo.response.FeedBackRepVO;
import com.eseasky.modules.order.vo.response.ListWithPage;
import com.eseasky.modules.order.vo.response.OrderMeetingListDetailRepVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户反馈 服务实现类
 * </p>
 *
 * @author
 * @since 2021-07-16
 */
@Service
@Slf4j
public class OrderFeedBackServiceImpl extends ServiceImpl<OrderFeedBackMapper, OrderFeedBack> implements OrderFeedBackService {

    @Resource
    private OrderFeedBackMapper orderFeedBackMapper;

    @Resource
    private OrderMeetingListMapper orderMeetingListMapper;

    @Resource
    private OrderSeatListMapper orderSeatListMapper;

    @Resource
    private OrderGroupListMapper orderGroupListMapper;

    @Resource
    private OrderGroupDetailMapper orderGroupDetailMapper;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public R<String> addOrderFreeBack(OrderFeedBackAddReqVO orderFeedBackAddReqVO) {
        log.info("OrderFeedBackServiceImpl.addOrderFreeBack msg: " + JSONObject.toJSONString(orderFeedBackAddReqVO));
        //获取操作用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        // 获取用户信息
        String userId = sysUserDTO.getId();
        String orderListId = orderFeedBackAddReqVO.getOrderListId();
        Integer orderType = orderFeedBackAddReqVO.getOrderType();
        CommonUtil.notNullOrEmpty(orderListId, "订单编号为空!");
        CommonUtil.notNull(orderType, "订单类型为空");
        if (OrderConstant.OrderFeedBackType.OTHER.equals(orderFeedBackAddReqVO.getType())) {
            CommonUtil.notNullOrEmpty(orderFeedBackAddReqVO.getContent(), "反馈类型为其他事,反馈内容不可为空");
        }
        String key = "feedback:addFeedBack:" + orderType + ":" + orderListId;
        String addFeedBackVal = redisRepository.get(key, String.class);
        long expire = redisRepository.getExpire(key);
        if (StringUtils.isNotBlank(addFeedBackVal) && expire > 0) {
            throw BusinessException.of("反馈过于频繁,请于" + (int) Math.ceil((double) expire / 60) + "分钟后再尝试");
        }

        OrderFeedBack orderFeedBack = null;
        //座位
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT, OrderConstant.OrderType.LONG_RENT).contains(orderType)) {
            OrderSeatListDTO orderSeatListDetail = orderSeatListMapper.getOrderSeatListDetail(orderListId);
            CommonUtil.notNull(orderSeatListDetail, "不存在此座位预约记录");
            orderFeedBack = OrderFeedBackUtils.getOrderFreeBackByFeedBackAddReqVOAndSeat(orderFeedBackAddReqVO, orderSeatListDetail, userId);
        }

        //拼团
        if (OrderConstant.OrderType.GROUP_RENT.equals(orderType)) {
            LambdaQueryWrapper<OrderGroupDetail> groupDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            groupDetailLambdaQueryWrapper.eq(OrderGroupDetail::getOrderGroupDetailId, orderListId);
            OrderGroupDetail orderGroupDetail = orderGroupDetailMapper.selectOne(groupDetailLambdaQueryWrapper);
            CommonUtil.notNull(orderGroupDetail, "不存在此订单记录");

            String orderGroupId = orderGroupDetail.getOrderGroupId();
            LambdaQueryWrapper<OrderGroupList> groupListLambdaQueryWrapper = new LambdaQueryWrapper<>();
            groupListLambdaQueryWrapper.eq(OrderGroupList::getOrderGroupId, orderGroupId);
            OrderGroupList orderGroupList = orderGroupListMapper.selectOne(groupListLambdaQueryWrapper);
            OrderGroupListDetailedDTO orderGroupListDetailed = orderGroupListMapper.getOrderGroupListDetailed(orderGroupId);
            CommonUtil.notNull(orderGroupList, "不存在此会议室预约订单");

            orderFeedBack = OrderFeedBackUtils.getOrderFreeBackByFeedBackAddReqVOAndGroup(orderFeedBackAddReqVO, orderGroupListDetailed, userId);
        }

        //会议室
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.LONG_RENT_ORDER_MEETING).contains(orderType)) {
            OrderMeetingListDetailRepVO orderDetail = orderMeetingListMapper.getOrderDetail(orderListId);
            CommonUtil.notNull(orderDetail, "不存在此订单");
            orderFeedBack = OrderFeedBackUtils.getOrderFreeBackByFeedBackAddReqVOAndMeeting(orderFeedBackAddReqVO, orderDetail, userId);
        }

        if (ObjectUtil.isNotNull(orderFeedBack)) {
            int count = orderFeedBackMapper.insert(orderFeedBack);
            CommonUtil.check(count > 0, "反馈失败");
            //5分钟可以反馈一次
            redisRepository.set(key, orderFeedBack.getFeedBackId(), 5 * 60);
            return R.ok("反馈成功");
        }
        return R.error("反馈失败");
    }

    @Override
    public R<ListWithPage<FeedBackRepVO>> showFeedBackPage(OrderFeedBackPageReqVO orderFeedBackPageReqVO) {
        log.info("OrderFeedBackServiceImpl.showFeedBackPage msg: " + JSONObject.toJSONString(orderFeedBackPageReqVO));
        LambdaQueryWrapper<OrderFeedBack> feedBackLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(orderFeedBackPageReqVO.getStartTime())) {
            feedBackLambdaQueryWrapper.ge(OrderFeedBack::getCreateTime, orderFeedBackPageReqVO.getStartTime());
        }
        if (ObjectUtil.isNotNull(orderFeedBackPageReqVO.getEndTime())) {
            feedBackLambdaQueryWrapper.le(OrderFeedBack::getCreateTime, orderFeedBackPageReqVO.getEndTime());
        }
        if (ObjectUtil.isNotNull(orderFeedBackPageReqVO.getType()) && 0 != orderFeedBackPageReqVO.getType()) {
            feedBackLambdaQueryWrapper.eq(OrderFeedBack::getType, orderFeedBackPageReqVO.getType());
        }
        if (StringUtils.isNotBlank(orderFeedBackPageReqVO.getBuildId())) {
            feedBackLambdaQueryWrapper.eq(OrderFeedBack::getBuildId, orderFeedBackPageReqVO.getBuildId());
        }
        feedBackLambdaQueryWrapper.eq(OrderFeedBack::getDelFlag, "0");
        if (ObjectUtil.isNotNull(orderFeedBackPageReqVO.getIsOrder()) && Arrays.asList(1, 2).contains(orderFeedBackPageReqVO.getIsOrder())) {
            if (1 == orderFeedBackPageReqVO.getIsOrder()) {
                feedBackLambdaQueryWrapper.orderByDesc(OrderFeedBack::getState);
            }
            if (2 == orderFeedBackPageReqVO.getIsOrder()) {
                feedBackLambdaQueryWrapper.orderByAsc(OrderFeedBack::getState);
            }
        } else {
            //默认升序
            feedBackLambdaQueryWrapper.orderByAsc(OrderFeedBack::getState);
        }
        feedBackLambdaQueryWrapper.orderByDesc(OrderFeedBack::getCreateTime);

        Page<OrderFeedBack> page = new Page<>(orderFeedBackPageReqVO.getPageNum(), orderFeedBackPageReqVO.getPageSize());
        Page<OrderFeedBack> feedBackPage = orderFeedBackMapper.selectPage(page, feedBackLambdaQueryWrapper);

        List<FeedBackRepVO> feedBackRepVOList = OrderFeedBackUtils.getFeedBackRepVOListByFeedBackList(feedBackPage.getRecords());

        ListWithPage<FeedBackRepVO> listWithPage = new ListWithPage<>();
        listWithPage.setTotal(Integer.valueOf(String.valueOf(feedBackPage.getTotal())));
        listWithPage.setDataList(feedBackRepVOList);
        listWithPage.setPages(Integer.valueOf(String.valueOf(feedBackPage.getPages())));

        return R.ok(listWithPage);
    }

    @Override
    public R<String> batchHandleFeedBack(OrderFeedBackHandReqVO orderFeedBackHandReqVO) {
        log.info("OrderFeedBackServiceImpl.batchHandleFeedBack msg: " + JSONObject.toJSONString(orderFeedBackHandReqVO));
        List<String> ids = orderFeedBackHandReqVO.getIds();
        CommonUtil.check(CollectionUtil.isNotEmpty(ids), "请选择用户反馈");
        //获取操作用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        // 获取用户信息
        String userId = sysUserDTO.getId();

//        List<OrderFeedBack> orderFeedBackList = orderFeedBackMapper.getOrderFeedBackByIds(ids);
//        List<OrderFeedBack> hasApproveList = orderFeedBackList.stream().filter(item -> OrderConstant.FeedBackState.YES.equals(item.getState())).collect(Collectors.toList());
//        CommonUtil.check(CollectionUtil.isEmpty(hasApproveList),"请勿重复处理");

        LambdaUpdateWrapper<OrderFeedBack> feedBackLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        feedBackLambdaUpdateWrapper.in(OrderFeedBack::getFeedBackId, ids);
        feedBackLambdaUpdateWrapper.set(OrderFeedBack::getState, OrderConstant.OrderFeedBackState.YES);
        feedBackLambdaUpdateWrapper.set(OrderFeedBack::getUpdateTime, new Date());
        feedBackLambdaUpdateWrapper.set(OrderFeedBack::getUpdateUserId, userId);
        orderFeedBackMapper.update(null, feedBackLambdaUpdateWrapper);

        return R.ok("处理成功");
    }

    @Override
    public R<List<JSONObject>> getBuildNameDropDownBox() {
        log.info("OrderFeedBackServiceImpl.getBuildNameDropDownBox");
        QueryWrapper<OrderFeedBack> orderFeedBackLambdaQueryWrapper = new QueryWrapper();
        orderFeedBackLambdaQueryWrapper.select("DISTINCT build_id, build_name");
        List<OrderFeedBack> orderFeedBackList = orderFeedBackMapper.selectList(orderFeedBackLambdaQueryWrapper);

        //返回的结果级别
        List<JSONObject> result = new ArrayList<>();

        for (OrderFeedBack orderFeedBack : orderFeedBackList) {
        JSONObject jsonObject = new JSONObject();
            jsonObject.put("label",orderFeedBack.getBuildId());
            jsonObject.put("value",orderFeedBack.getBuildName());
            result.add(jsonObject);
        }
        return R.ok(result);
    }
}
