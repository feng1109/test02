package com.eseasky.modules.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.entity.OrderComment;
import com.eseasky.modules.order.entity.OrderGroupDetail;
import com.eseasky.modules.order.entity.OrderMeetingList;
import com.eseasky.modules.order.entity.OrderSeatList;
import com.eseasky.modules.order.mapper.OrderCommentMapper;
import com.eseasky.modules.order.mapper.OrderGroupDetailMapper;
import com.eseasky.modules.order.mapper.OrderMeetingListMapper;
import com.eseasky.modules.order.mapper.OrderSeatListMapper;
import com.eseasky.modules.order.service.OrderCommentService;
import com.eseasky.modules.order.vo.request.ListCommentReqVO;
import com.eseasky.modules.order.vo.request.RoomCommentReqVO;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.service.SpaceRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2021-05-10
 */
@Service
public class OrderCommentServiceImpl extends ServiceImpl<OrderCommentMapper, OrderComment> implements OrderCommentService {

    @Autowired
    OrderCommentMapper orderCommentMapper;

    @Autowired
    OrderSeatListMapper orderSeatListMapper;

    @Autowired
    OrderGroupDetailMapper orderGroupDetailMapper;

    @Autowired
    OrderMeetingListMapper orderMeetingListMapper;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    SpaceRoomService roomService;

    /**
     * @return com.eseasky.common.code.utils.R
     * @description: 预约订单评价
     * @author: lc
     * @date: 2021/5/10 10:48
     * @params [listCommentReqVO]
     */
    @Override
    public R<String> commentList(ListCommentReqVO vo) {

        SpaceRoom room = roomService.getById(vo.getRoomId());
        if (room == null) {
            throw BusinessException.of("此空间已不存在！");
        }

        // 获取传参信息
        Integer orderType = vo.getOrderType();

        // 获取用户信息
        SysUserDTO sysUserDTO = SecurityUtils.getUser().getSysUserDTO();
        String userId = sysUserDTO.getId();
        String userName = sysUserDTO.getUsername();

        // 初始化评论信息
        OrderComment orderComment = JSONObject.parseObject(JSON.toJSONString(vo), OrderComment.class);
        orderComment.setBuildId(room.getBuildId())
                .setFloorId(room.getFloorId())
                .setCommentTime(new Date())
                .setUserId(userId)
                .setUserName(userName).
                setDelFlag("0");
        orderCommentMapper.insert(orderComment);

        // 订单类型为1,2 将单人订单改为已评论状态
        if (orderType.equals(OrderConstant.OrderType.SHORT_RENT) || orderType.equals(OrderConstant.OrderType.LONG_RENT)) {
            LambdaUpdateWrapper<OrderSeatList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderSeatList::getIsComment, OrderConstant.Switch.YES).eq(OrderSeatList::getOrderSeatId, vo.getOrderListId());
            orderSeatListMapper.update(null, updateWrapper);
        }

        // 订单类型为5 将拼团订单改为已评论状态
        if (orderType.equals(OrderConstant.OrderType.GROUP_RENT)) {
            LambdaUpdateWrapper<OrderGroupDetail> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderGroupDetail::getIsComment, OrderConstant.Switch.YES).eq(OrderGroupDetail::getOrderGroupDetailId, vo.getOrderListId());
            orderGroupDetailMapper.update(null, updateWrapper);
        }

        // 订单类型为3,4 将会议订单改为已评论状态
        if (Arrays.asList(OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING, OrderConstant.OrderType.SHORT_RENT_ORDER_MEETING).contains(orderType)) {
            LambdaUpdateWrapper<OrderMeetingList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderMeetingList::getIsComment, OrderConstant.Switch.YES).eq(OrderMeetingList::getOrderMeetingId, vo.getOrderListId());
            orderMeetingListMapper.update(null, updateWrapper);
        }


        HashOperations<String, Object, Object> forHash = redisTemplate.opsForHash();
        forHash.increment(OrderConstant.COMMENT_PERSON + room.getBuildId(), room.getRoomId(), 1);
        forHash.increment(OrderConstant.COMMENT_LEVEL + room.getBuildId(), room.getRoomId(), vo.getLevel());

        return R.ok("评论成功");
    }

    /**
     * @description: 展示空间评论
     * @author: lc
     * @date: 2021/5/10 11:19
     * @params [listCommentReqVO]
     * @return com.eseasky.common.code.utils.R
     */
    @Override
    public R showListComment(RoomCommentReqVO roomCommentReqVO) {

        // 获取传参参数
        String roomId = roomCommentReqVO.getRoomId();
        Integer sortType = roomCommentReqVO.getSortType();

        // 拼接查询条件
        LambdaQueryWrapper<OrderComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(OrderComment::getOrderCommentId, OrderComment::getUserName, OrderComment::getContent, OrderComment::getCommentTime,
                OrderComment::getLevel).eq(OrderComment::getRoomId, roomId).orderByDesc(OrderComment::getCommentTime);
        if (sortType.equals(OrderConstant.sortType.CREATE_TIME_ASC)) {
            queryWrapper.orderByAsc(OrderComment::getCommentTime);
        }
        if (sortType.equals(OrderConstant.sortType.CREATE_TIME_DESC)) {
            queryWrapper.orderByDesc(OrderComment::getCommentTime);
        }

        Page<OrderComment> page = new Page<>(roomCommentReqVO.getPageNum(), roomCommentReqVO.getPageSize());
        Page<OrderComment> orderCommentPage = orderCommentMapper.selectPage(page, queryWrapper);

        return R.ok(orderCommentPage);

    }

    /**
     * @description: 删除评论
     * @author: lc
     * @date: 2021/5/10 12:00
     * @params [roomCommentReqVO]
     * @return com.eseasky.common.code.utils.R
     */
    @Override
    public R delListComment(String orderCommentId) {
        orderCommentMapper.deleteById(orderCommentId);
        return R.ok("删除评论成功");
    }

    /**
     * @description: 删除空间时级联删除评论 1.建筑id 2.楼层id 3.房间id
     * @author: lc
     * @date: 2021/7/23 14:13
     * @params
     * @return
     */
    @Override
    public void delCommentBySpace(List<String> ids, Integer type) {

        LambdaUpdateWrapper<OrderComment> updateWrapper = new LambdaUpdateWrapper<>();

        // 根据类型id，拼接删除条件
        switch (type) {
            case 1:
                updateWrapper.in(OrderComment::getBuildId, ids);
                break;
            case 2:
                updateWrapper.in(OrderComment::getFloorId, ids);
                break;
            case 3:
                updateWrapper.in(OrderComment::getRoomId, ids);
                break;
            default:
                return;
        }

        orderCommentMapper.delete(updateWrapper);

    }


}
