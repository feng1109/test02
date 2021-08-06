package com.eseasky.modules.space.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.config.UploadProp;
import com.eseasky.modules.space.entity.SpaceBuild;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.entity.SpaceDesk;
import com.eseasky.modules.space.entity.SpaceFloor;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceBuildMapper;
import com.eseasky.modules.space.mapper.SpaceFloorMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.mapper.SpaceSeatMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceDeskService;
import com.eseasky.modules.space.service.SpaceFloorService;
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.SpaceFloorVO;
import com.eseasky.modules.space.vo.SpaceRoomVO;
import com.eseasky.modules.space.vo.request.EditRoomStateBatchVO;
import com.eseasky.modules.space.vo.request.ImportRoomImageVO;
import com.eseasky.modules.space.vo.request.QueryMobileRoomParam;
import com.eseasky.modules.space.vo.request.QueryOrderRoomParam;
import com.eseasky.modules.space.vo.request.QueryRoomParam;
import com.eseasky.modules.space.vo.request.QueryStatisticsRoomParam;
import com.eseasky.modules.space.vo.response.ConfIdVO;
import com.eseasky.modules.space.vo.response.DropDownVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.PageListVO;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 空间服务实现类
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Slf4j
@Service
public class SpaceRoomServiceImpl extends ServiceImpl<SpaceRoomMapper, SpaceRoom> implements SpaceRoomService {

    @Autowired
    private SpaceBuildService buildService;
    @Autowired
    private SpaceFloorService floorService;
    @Autowired
    private SpaceDeskService deskService;
    @Autowired
    private SpaceSeatService seatService;
    @Autowired
    private SpaceConfService confService;
    @Autowired
    private SpaceGroupService groupService;
    @Autowired
    private UploadProp prop;
    @Autowired
    private SpaceUtil spaceUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public R<String> addRoom(SpaceRoomVO spaceRoomVO) {
        SpaceFloor floor = floorService.getById(spaceRoomVO.getFloorId());
        if (floor == null) {
            log.error("SpaceRoomServiceImpl-addRoom，获取楼层信息失败，spaceRoomVO：{}", JSON.toJSONString(spaceRoomVO));
            return R.error("获取楼层信息失败！");
        }

        SpaceBuild build = buildService.getById(spaceRoomVO.getBuildId());
        if (build == null) {
            log.error("SpaceRoomServiceImpl-addRoom，获取综合楼信息失败，spaceRoomVO：{}", JSON.toJSONString(spaceRoomVO));
            return R.error("获取综合楼信息失败！");
        }

        SpaceRoom room = JSONObject.parseObject(JSON.toJSONString(spaceRoomVO), SpaceRoom.class);
        room.setCreateTime(new Date());
        room.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
        room.setRoomImage(spaceUtil.deleteIpAndPort(room.getRoomImage()));
        room.setRoomState(SpaceConstant.Switch.OPEN); // 新建的空间本身状态是开放，但是房间的父级状态要和综合楼状态保持一致

        if (!save(room)) {
            log.error("SpaceRoomServiceImpl-addRoom，楼层保存失败，spaceRoomVO：{}", JSON.toJSONString(spaceRoomVO));
            throw BusinessException.of("楼层保存失败！");
        }

        return R.ok("保存成功！");
    }

    @Override
    @Transactional
    public R<String> deleteRoom(String roomId) {
        SpaceRoom roomToLog = getById(roomId);
        if (roomToLog == null) {
            throw BusinessException.of("该空间不存在！");
        }

        // 逻辑删除
        removeById(roomId);
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().eq(SpaceDesk::getRoomId, roomId));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().eq(SpaceSeat::getRoomId, roomId));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().eq(SpaceGroup::getRoomId, roomId));

        // 根据房间id取消订单
        buildService.cancelOrder(Lists.newArrayList(roomId), SpaceConstant.SpaceType.ROOM);
        return R.ok("删除成功！");
    }


    @Override
    @Transactional
    public R<String> deleteRoomBatch(JSONObject param) {
        List<String> roomIdList = JSONArray.parseArray(param.getJSONArray("roomIdList").toJSONString(), String.class);
        if (CollectionUtils.isEmpty(roomIdList)) {
            return R.error("获取参数失败！");
        }

        // 逻辑删除
        removeByIds(roomIdList);
        deskService.remove(new QueryWrapper<SpaceDesk>().lambda().in(SpaceDesk::getRoomId, roomIdList));
        seatService.remove(new QueryWrapper<SpaceSeat>().lambda().in(SpaceSeat::getRoomId, roomIdList));
        groupService.remove(new QueryWrapper<SpaceGroup>().lambda().in(SpaceGroup::getRoomId, roomIdList));

        // 根据房间id取消订单
        buildService.cancelOrder(roomIdList, SpaceConstant.SpaceType.ROOM);
        return R.ok("删除成功！");
    }

    @Override
    @Transactional
    public R<String> editRoom(SpaceRoomVO spaceRoomVO) {
        SpaceRoom oldRoom = getById(spaceRoomVO.getRoomId());
        if (oldRoom == null) {
            log.error("SpaceRoomServiceImpl-editRoom，获取空间信息失败，spaceRoomVO：{}", JSON.toJSONString(spaceRoomVO));
            return R.error("获取空间信息失败！");
        }

        SpaceRoom newRoom = JSONObject.parseObject(JSON.toJSONString(spaceRoomVO), SpaceRoom.class);
        lambdaUpdate().set(SpaceRoom::getRoomName, newRoom.getRoomName()) //
                .set(SpaceRoom::getRoomNum, newRoom.getRoomNum()) //
                .set(SpaceRoom::getRoomState, newRoom.getRoomState()) //
                .set(SpaceRoom::getArea, newRoom.getArea())//
                .set(SpaceRoom::getUpdateTime, new Date())//
                .set(SpaceRoom::getUpdateUser, SecurityUtils.getUser().getSysUserDTO().getId())//
                .eq(SpaceRoom::getRoomId, spaceRoomVO.getRoomId())//
                .update();


        changeStateByNewRoomAndOldRoom(newRoom, oldRoom);


        // 原状态是开放，修改后的状态是关闭
        if (oldRoom.getRoomState().intValue() == SpaceConstant.Switch.OPEN) {
            if (newRoom.getRoomState().intValue() == SpaceConstant.Switch.CLOSE) {
                // 取消订单
                buildService.cancelOrder(Lists.newArrayList(newRoom.getRoomId()), SpaceConstant.SpaceType.ROOM);
            }
        }
        return R.ok("修改成功！");
    }


    private void changeStateByNewRoomAndOldRoom(SpaceRoom newRoom, SpaceRoom oldRoom) {
        // 修改后的空间状态不一致
        if (oldRoom.getRoomState().intValue() != newRoom.getRoomState().intValue()) {

            // 空间变为关闭，是将空间下的所有座位变为3，但4除外
            if (newRoom.getRoomState().intValue() == SpaceConstant.Switch.CLOSE) {
                seatService.lambdaUpdate() //
                        .set(SpaceSeat::getSeatState, SpaceConstant.Switch.CLOSE) //
                        .set(SpaceSeat::getParentState, SpaceConstant.SpaceType.ROOM) //
                        .eq(SpaceSeat::getRoomId, newRoom.getRoomId()) //
                        .le(SpaceSeat::getParentState, SpaceConstant.SpaceType.ROOM) //
                        .update();
            }

            // 空间变为开放，说明原来空间下的所有座位都是禁用【3，4】
            if (newRoom.getRoomState().intValue() == SpaceConstant.Switch.OPEN) {

                // 如果综合楼也是开放，就将除了4之外的都开放
                SpaceBuild build = buildService.getById(oldRoom.getBuildId());
                if (build.getBuildState().intValue() == SpaceConstant.Switch.OPEN) {
                    seatService.lambdaUpdate() //
                            .set(SpaceSeat::getSeatState, SpaceConstant.Switch.OPEN) //
                            .set(SpaceSeat::getParentState, SpaceConstant.SpaceType.NONE) //
                            .eq(SpaceSeat::getRoomId, newRoom.getRoomId()) //
                            .le(SpaceSeat::getParentState, SpaceConstant.SpaceType.ROOM) //
                            .update();
                }
                // 空间开放，综合楼却不开放。将除了4之外的，都改为1。
                else {
                    seatService.lambdaUpdate() //
                            .set(SpaceSeat::getSeatState, SpaceConstant.Switch.CLOSE) //
                            .set(SpaceSeat::getParentState, SpaceConstant.SpaceType.BUILD) //
                            .eq(SpaceSeat::getRoomId, newRoom.getRoomId()) //
                            .le(SpaceSeat::getParentState, SpaceConstant.SpaceType.ROOM) //
                            .update();
                }
            }
        }
    }


    @Override
    @Transactional
    public R<String> modifyConfId(JSONObject param) {
        String roomId = param.getString("roomId");
        String confId = param.getString("confId");
        if (StringUtils.isBlank(roomId) || StringUtils.isBlank(confId)) {
            log.error("SpaceRoomServiceImpl-modifyConfId，获取参数失败，roomId：{}，confId：{}", roomId, confId);
            throw BusinessException.of("获取参数失败！");
        }

        SpaceRoom room = getById(roomId);
        if (room == null) {
            log.error("SpaceRoomServiceImpl-modifyConfId，获取房间失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取房间失败！");
        }

        SpaceConf conf = confService.lambdaQuery().eq(SpaceConf::getConfId, confId).one();
        if (conf == null) {
            log.error("SpaceRoomServiceImpl-modifyConfId，获取配置规则失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取配置规则失败！");
        }

        // 根据roomId更新空间
        lambdaUpdate() //
                .set(SpaceRoom::getConfId, confId) //
                .set(SpaceRoom::getConfUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceRoom::getConfTime, new Date()) //
                .eq(SpaceRoom::getRoomId, roomId) //
                .update();


        // ParentConf <= 3，【0没有，1综合楼，2楼层，空间】
        seatService.lambdaUpdate()//
                .set(SpaceSeat::getConfId, confId)//
                .set(SpaceSeat::getParentConf, SpaceConstant.SpaceType.ROOM) //
                .eq(SpaceSeat::getRoomId, roomId) //
                .le(SpaceSeat::getParentConf, SpaceConstant.SpaceType.ROOM) //
                .update();

        return R.ok("修改成功！");
    }


    @Override
    @Transactional
    public R<PageListVO<OneBOneFOneR>> getSpaceRoomList(QueryRoomParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        String buildId = param.getBuildId();
        String floorId = param.getFloorId();
        String roomName = param.getRoomName();

        Set<String> orgIdList = spaceUtil.getOrgIdList();

        Page<OneBOneFOneR> page = new Page<OneBOneFOneR>(pageNum, pageSize);
        List<OneBOneFOneR> findRoomList = getBaseMapper().findRoomList(page, buildId, floorId, roomName, orgIdList);

        PageListVO<OneBOneFOneR> result = new PageListVO<OneBOneFOneR>();
        result.setList(findRoomList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    /**
     * 每次课桌的数量变动或者座位的数量变动，都要修改空间的数量属性。不冗余此字段涉及到数量的地方都要单独查询。
     */
    @Override
    @Transactional
    public boolean modifySeatCountAndDeskCount(String roomId, Integer seatCount, Integer seatNotForbidCount) {
        if (seatCount == null) {
            return false;
        }
        return lambdaUpdate() //
                .set(SpaceRoom::getSeatCount, seatCount) //
                .set(SpaceRoom::getSeatNotForbidCount, seatNotForbidCount) //
                .eq(SpaceRoom::getRoomId, roomId).update();
    }

    @Override
    @Transactional
    public R<SpaceRoomVO> findOneRoom(String roomId) {
        SpaceRoom room = getById(roomId);
        if (room == null) {
            log.error("SpaceRoomServiceImpl-findOneRoom，获取空间信息失败，roomId：{}", roomId);
            throw BusinessException.of("获取空间信息失败！");
        }

        SpaceRoomVO vo = JSONObject.parseObject(JSON.toJSONString(room), SpaceRoomVO.class);
        vo.setRoomImage(spaceUtil.addIpAndPort(vo.getRoomImage(), prop.getIpPort()));
        return R.ok(vo);
    }


    /**
     * 统计中心：房间展示列表
     */
    @Override
    @Transactional
    public R<PageListVO<SpaceRoomVO>> getStatisticRoomList(QueryStatisticsRoomParam param) {
        long pageSize = param.getPageSize();
        long pageNum = param.getPageNum();
        String buildId = param.getBuildId();
        String floorId = param.getFloorId();
        String roomName = param.getRoomName();
        int type = param.getType(); // 1座位，3会议室


        // 第一：根据type选出座位规则和会议室规则
        List<String> confIdListToQuery = new ArrayList<String>();
        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (MapUtils.isEmpty(confIdAndSpaceConf)) {
            return aa(pageSize, pageNum, "获取规则失败！");
        }
        Collection<SpaceConfVO> allConfList = confIdAndSpaceConf.values();
        for (SpaceConfVO spaceConfVO : allConfList) {
            Integer orderType = spaceConfVO.getOrderType();
            if (orderType == null) {
                continue;
            }

            // 1座位
            if (type == SpaceConstant.OrderType.SINGLE_ONCE) {
                if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG) {
                    confIdListToQuery.add(spaceConfVO.getConfId());
                }
            }
            // 3会议室
            else if (type == SpaceConstant.OrderType.MULTI_ONCE) {
                if (orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_LONG) {
                    confIdListToQuery.add(spaceConfVO.getConfId());
                }
            }
        }
        if (CollectionUtils.isEmpty(confIdListToQuery)) {
            return aa(pageSize, pageNum, "获取规则失败！");
        }


        // 第二：再匹配空间名称，没有直接返回
        Set<String> roomIdFromRoomName = new HashSet<>();
        if (StringUtils.isNotBlank(roomName)) {
            List<Object> list = getBaseMapper().selectObjs(new QueryWrapper<SpaceRoom>().lambda() //
                    .select(SpaceRoom::getRoomId) //
                    .like(SpaceRoom::getRoomName, roomName));

            if (CollectionUtils.isEmpty(list)) {
                return aa(pageSize, pageNum, "根据空间名称未查询到空间！");
            }
            roomIdFromRoomName = list.stream().map(m -> m.toString()).collect(Collectors.toSet());
        }


        // 第三：根据配置规则获得所有对应类型的空间id，用于分页
        LambdaQueryWrapper<SpaceSeat> wrapper = new QueryWrapper<SpaceSeat>().lambda();
        wrapper.select(SpaceSeat::getRoomId);
        wrapper.eq(SpaceSeat::getBuildId, buildId);
        if (StringUtils.isNotBlank(floorId)) {
            wrapper.eq(SpaceSeat::getFloorId, floorId);
        }
        if (!CollectionUtils.isEmpty(roomIdFromRoomName)) {
            wrapper.in(SpaceSeat::getRoomId, roomIdFromRoomName);
        }
        wrapper.in(SpaceSeat::getConfId, confIdListToQuery);
        List<Object> roomIdObj = seatService.getBaseMapper().selectObjs(wrapper);
        Set<String> roomIdList = roomIdObj.stream().map(m -> m.toString()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(roomIdList)) {
            return aa(pageSize, pageNum, "根据规则未查询到空间！");
        }


        // 第四：对可见的roomIdList进行分页
        Page<SpaceRoom> page = new Page<SpaceRoom>(pageNum, pageSize);
        LambdaQueryChainWrapper<SpaceRoom> lambdaQuery = lambdaQuery();
        lambdaQuery.eq(SpaceRoom::getBuildId, buildId);
        if (StringUtils.isNotBlank(floorId)) {
            lambdaQuery.eq(SpaceRoom::getFloorId, floorId);
        }
        lambdaQuery.in(SpaceRoom::getRoomId, roomIdList);
        lambdaQuery.orderByAsc(SpaceRoom::getRoomNum);
        lambdaQuery.page(page);
        // 分页、排序获得的空间集合，用于在订单表获得当前时刻该空间正在使用的座位数量
        List<SpaceRoom> records = page.getRecords();
        roomIdList = records.stream().map(m -> m.getRoomId()).collect(Collectors.toSet());


        // 第五：是房间就统计该房间有多少个座位在使用中，是会议室就判断该会议室是否在使用中
        Map<String, Integer> roomIdAndSeatInUsedCount = null;
        List<String> inUsedMeetingRoom = null;
        // 1座位
        if (type == SpaceConstant.OrderType.SINGLE_ONCE) {
            roomIdAndSeatInUsedCount = spaceUtil.roomIdAndSeatInUsedCount(new Date(), roomIdList);
        }
        // 3会议室
        else if (type == SpaceConstant.OrderType.MULTI_ONCE) {
            inUsedMeetingRoom = spaceUtil.getMeetingRoomInUsedList(new Date(), roomIdList);
        }


        // 第六：结果集封装
        SpaceBuild build = buildService.getById(buildId);
        List<SpaceRoomVO> temRoomVOList = new ArrayList<>();
        for (SpaceRoom room : records) {
            SpaceRoomVO vo = new SpaceRoomVO();
            temRoomVOList.add(vo);

            vo.setBuildId(room.getBuildId());
            vo.setFloorId(room.getFloorId());
            vo.setRoomId(room.getRoomId());
            vo.setRoomName(room.getRoomName());
            vo.setRoomNum(room.getRoomNum());
            vo.setRoomImage(spaceUtil.addIpAndPort(room.getRoomImage(), prop.getIpPort()));
            vo.setSeatCount(room.getSeatCount());

            // 先要判断综合楼、该房间是否禁用，如果禁用，座位总数不变，房间可用座位是0、使用中的座位是0、会议室的使用状态是否
            if (build.getBuildState() == SpaceConstant.Switch.CLOSE || room.getRoomState() == SpaceConstant.Switch.CLOSE) {
                vo.setRoomState(SpaceConstant.Switch.CLOSE);
                vo.setSeatInUsedCount(0);
                vo.setSeatNotUsedCount(room.getSeatCount());
                vo.setIsMeetingRoomInUsed(SpaceConstant.Switch.NO);
                continue;
            }

            // 1座位
            if (type == SpaceConstant.OrderType.SINGLE_ONCE) {
                Integer seatInUsedCount = roomIdAndSeatInUsedCount.get(room.getRoomId());
                if (seatInUsedCount == null) {
                    vo.setSeatInUsedCount(0);
                    vo.setSeatNotUsedCount(room.getSeatNotForbidCount());
                } else {
                    vo.setSeatInUsedCount(seatInUsedCount.intValue());
                    vo.setSeatNotUsedCount(room.getSeatNotForbidCount() - seatInUsedCount.intValue());
                }
            }
            // 3会议室
            else if (type == SpaceConstant.OrderType.MULTI_ONCE) {
                if (inUsedMeetingRoom.contains(room.getRoomId())) {
                    vo.setIsMeetingRoomInUsed(SpaceConstant.Switch.YES);
                } else {
                    vo.setIsMeetingRoomInUsed(SpaceConstant.Switch.NO);
                }
            }
        }

        PageListVO<SpaceRoomVO> result = new PageListVO<SpaceRoomVO>();
        result.setList(temRoomVOList);
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return R.ok(result);
    }

    // 返回列表为空，但说明为什么没数据
    private R<PageListVO<SpaceRoomVO>> aa(long pageSize, long pageNum, String msg) {
        PageListVO<SpaceRoomVO> result = new PageListVO<SpaceRoomVO>();
        result.setList(new ArrayList<>());
        result.setTotal(0);
        result.setPages(0);
        result.setPageSize(pageSize);
        result.setPageNum(pageNum);
        return new R<PageListVO<SpaceRoomVO>>(R.SUCCESS_CODE, msg, result);
    }


    @Override
    @Transactional
    public R<OneBOneFOneR> getRoomInfoForOrder(QueryOrderRoomParam param) {
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();
        Integer orderType = param.getOrderType();
        String roomId = param.getRoomId();

        if (orderType == null) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "缺少预约类型！", null);
        }
        if (startDate == null || endDate == null) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "缺少预约时间！", null);
        }

        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE) {
            if (startDate.getTime() < new Date().getTime()) {
                return new R<OneBOneFOneR>(R.FAIL_CODE, "预约时间不得小于当前时间！", null);
            }
        }

        String confIdToUse = null;
        OneBOneFOneR room = getBaseMapper().getOneBuildOneFloorOneRoom(roomId);
        if (room.getRoomState() == SpaceConstant.Switch.CLOSE) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "该空间已关闭！", null);
        }
        if (room.getBuildState() == SpaceConstant.Switch.CLOSE) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "该场馆已关闭！", null);
        }
        if (StringUtils.isNotBlank(room.getBuildConfId())) {
            confIdToUse = room.getBuildConfId();
        }
        if (StringUtils.isNotBlank(room.getFloorConfId())) {
            confIdToUse = room.getFloorConfId();
        }
        if (StringUtils.isNotBlank(room.getRoomConfId())) {
            confIdToUse = room.getRoomConfId();
        }
        if (confIdToUse == null) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "获取配置规则失败！", null);
        }

        // 查询该空间规则列表。用来判断空间的开放对象是否符合当前用户，开放时间是否符合预约时间段。
        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "获取配置信息失败！", null);
        }
        SpaceConfVO conf = confIdAndSpaceConf.get(confIdToUse);
        if (conf == null) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, "获取配置信息失败！", null);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String startTime = sdf.format(startDate);
        String endTime = sdf.format(endDate);

        // 短租需要判断开放时间
        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE) {
            String judgeConf = spaceUtil.judgeOpenTime(conf, startTime, endTime, startDate, endDate, orderType);
            if (judgeConf != null) {
                return new R<OneBOneFOneR>(R.FAIL_CODE, judgeConf, null);
            }
        }

        // 长租和短租都要判断开放对象
        String showRoom = spaceUtil.showRoom(conf, orderType);
        if (showRoom != null) {
            return new R<OneBOneFOneR>(R.FAIL_CODE, showRoom, null);
        }

        return R.ok(room);
    }


    @Override
    @Transactional
    public R<SpaceConfVO> getRoomConfForOrder(String roomId) {
        String confIdToUse = null;
        OneBOneFOneR room = getBaseMapper().getOneBuildOneFloorOneRoom(roomId);
        if (StringUtils.isNotBlank(room.getBuildConfId())) {
            confIdToUse = room.getBuildConfId();
        }
        if (StringUtils.isNotBlank(room.getFloorConfId())) {
            confIdToUse = room.getFloorConfId();
        }
        if (StringUtils.isNotBlank(room.getRoomConfId())) {
            confIdToUse = room.getRoomConfId();
        }
        if (confIdToUse == null) {
            return new R<SpaceConfVO>(R.FAIL_CODE, "获取配置规则失败！", null);
        }

        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null) {
            return new R<SpaceConfVO>(R.FAIL_CODE, "获取配置信息失败！", null);
        }
        SpaceConfVO conf = confIdAndSpaceConf.get(confIdToUse);
        if (conf == null) {
            return new R<SpaceConfVO>(R.FAIL_CODE, "获取配置信息失败！", null);
        }

        return R.ok(conf);
    }


    /**
     * 手机端--空间列表展示
     */
    @Override
    @Transactional
    public R<List<SpaceFloorVO>> getMobileRoomList(QueryMobileRoomParam param) {
        Integer closeWindow = param.getCloseWindow();
        Integer haveSocket = param.getHaveSocket();
        Integer awayToilet = param.getAwayToilet();
        Integer awayDoor = param.getAwayDoor();
        String buildId = param.getBuildId();
        String roomName = param.getRoomName();
        Integer orderType = param.getOrderType(); // 1短租 2长租
        String sDate = param.getStartDate();
        String eDate = param.getEndDate();
        String startTime = param.getStartTime(); // HH:mm
        String endTime = param.getEndTime(); // HH:mm，非手机端参数，下面判断用到

        Date startDate = null;
        Date endDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            startDate = sdf.parse(sDate + " " + startTime);
            endDate = sdf.parse(eDate + " " + endTime);
        } catch (Exception e) {
            log.error("SpaceRoomServiceImpl-getMobileRoomList，日期格式错误，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("日期格式错误！");
        }


        /** 规则筛选 */
        ConfIdVO confIdVO = spaceUtil.getConfIdListForCurrentUser(startTime, endTime, startDate, endDate, orderType);
        List<String> openList = confIdVO.getOpenList();
        List<String> freeList = confIdVO.getFreeList();
        if (CollectionUtils.isEmpty(openList)) {
            log.error("SpaceRoomServiceImpl-getMobileRoomList，暂无适合您的空间-根据日期时间过滤无配置规则！");
            return new R<List<SpaceFloorVO>>(new ArrayList<SpaceFloorVO>(), SpaceConstant.MOBILE_NO_SPACE);
        }


        /** 根据综合楼和规则，查询对当前用户可见的房间。 */
        List<Object> roomIdObj = seatService.getBaseMapper().selectObjs(new QueryWrapper<SpaceSeat>().lambda() //
                .select(SpaceSeat::getRoomId) //
                .in(SpaceSeat::getConfId, openList) //
                .eq(SpaceSeat::getBuildId, buildId) //
                .groupBy(SpaceSeat::getRoomId));
        Set<String> roomIdListCanSee = roomIdObj.stream().map(m -> m.toString()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(roomIdListCanSee)) {
            log.error("SpaceRoomServiceImpl-getMobileRoomList，暂无适合您的空间-可见规则未查到可见房间！");
            return new R<List<SpaceFloorVO>>(new ArrayList<SpaceFloorVO>(), SpaceConstant.MOBILE_NO_SPACE);
        }


        /** 根据综合楼和规则，查询对当前用户可预约的房间和房间内的座位总数。 */
        Map<String, Integer> roomIdAndSeatNotUsedTotal = new HashMap<>();
        if (!CollectionUtils.isEmpty(freeList)) {
            List<JSONObject> list = null;
            if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG) {
                // 可预约confId、可见seat、非删除seat，并且是分页buildId，排除时间段内占用的seatId
                list = ((SpaceSeatMapper) seatService.getBaseMapper()).singleOrderForRoom(freeList, buildId, startDate, endDate, orderType);
            } else {
                list = ((SpaceSeatMapper) seatService.getBaseMapper()).multiOrderForRoom(freeList, buildId, startDate, endDate, orderType);
            }
            for (JSONObject map : list) {
                roomIdAndSeatNotUsedTotal.put(map.getString("roomId"), map.getIntValue("seatNotUsedTotal"));
            }
        }


        /** 如果是单人短租，并且勾选了座位属性，就筛选房间，清空可见集合 */
        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE) {
            List<String> roomIdbyTag = spaceUtil.filterRoomIdByTag(buildId, closeWindow, haveSocket, awayToilet, awayDoor);
            if (roomIdbyTag == null) {
                // 手机端未勾选任何过滤条件
            } else if (roomIdbyTag.size() == 0) {
                // 手机端勾选了过滤条件，但是没查到数据
                roomIdListCanSee.clear();
            } else {
                // 手机端勾选了过滤条件，并且查到数据，取交集。可见和可预约保持一致。
                Set<String> roomIdListCanOrder = roomIdAndSeatNotUsedTotal.keySet();
                roomIdListCanOrder.retainAll(roomIdbyTag);
                roomIdListCanSee = roomIdListCanOrder;
            }

            if (CollectionUtils.isEmpty(roomIdListCanSee)) {
                log.debug("SpaceRoomServiceImpl-getMobileRoomList，暂无适合您的空间-座位属性过滤之后无可预约空间！");
                return new R<List<SpaceFloorVO>>(new ArrayList<SpaceFloorVO>(), SpaceConstant.MOBILE_NO_SPACE);
            }
        }


        /** 如果手机端是高评分，就计算每个房间的平均评分 */
        Map<String, Double> roomIdAndAvgscore = new HashMap<String, Double>(); // 房间id和每个房间的平均评分
        List<String> roomIdOrderByScoreDesc = new ArrayList<String>(); // 根据评分从高到低排序的房间id集合
        if (param.getHighScore() == SpaceConstant.Switch.YES) {
            HashOperations<String, Object, Object> forHash = redisTemplate.opsForHash();
            String personKey = OrderConstant.COMMENT_PERSON + buildId;
            String levelKey = OrderConstant.COMMENT_LEVEL + buildId;

            TreeMultimap<Double, String> sortedAvgscoreAndRoomId = TreeMultimap.create();
            for (String rId : roomIdListCanSee) {
                Object person = forHash.get(personKey, rId);
                if (person == null) {
                    roomIdAndAvgscore.put(rId, 0.0);
                    sortedAvgscoreAndRoomId.put(0.0, rId);
                    continue;
                }
                Object level = forHash.get(levelKey, rId);
                double avg = Double.valueOf(level.toString()) / Double.valueOf(person.toString());
                roomIdAndAvgscore.put(rId, avg);
                sortedAvgscoreAndRoomId.put(avg, rId);
            }

            roomIdOrderByScoreDesc = Lists.newArrayList(sortedAvgscoreAndRoomId.values());
            Collections.reverse(roomIdOrderByScoreDesc);
        }



        // allRooms 是整栋楼里面的所有楼层和空间，temFloorVOList是整栋楼里面的所有楼层和对用户的可预约空间
        // 对list循环，保留对用户可预约的room并放入roomVO
        List<SpaceFloor> allRooms = ((SpaceFloorMapper) floorService.getBaseMapper()).findFloorList(buildId, roomName);
        List<SpaceFloorVO> temFloorVOList = new ArrayList<>();
        for (SpaceFloor floor : allRooms) {

            List<SpaceRoomVO> temRoomVOList = new ArrayList<SpaceRoomVO>(20);
            Map<String, SpaceRoomVO> roomIdAndRoomVO = new TreeMap<String, SpaceRoomVO>();
            for (SpaceRoom room : floor.getRoomList()) {

                // 不可见的空间直接跳过
                if (!roomIdListCanSee.contains(room.getRoomId())) {
                    continue;
                }

                SpaceRoomVO vo = new SpaceRoomVO();
                BeanUtils.copyProperties(room, vo);
                vo.setRoomImage(spaceUtil.addIpAndPort(vo.getRoomImage(), prop.getIpPort()));
                if (vo.getRoomState().intValue() == SpaceConstant.Switch.CLOSE) {
                    vo.setSeatNotUsedCount(0);
                } else {
                    // 计算可用的座位数
                    Integer seatNotUsedTotal = roomIdAndSeatNotUsedTotal.get(vo.getRoomId());
                    vo.setSeatNotUsedCount(seatNotUsedTotal == null ? 0 : seatNotUsedTotal);
                }

                roomIdAndRoomVO.put(room.getRoomId(), vo);
                temRoomVOList.add(vo);
            }

            // 没有房间的楼层不展示
            if (CollectionUtils.isEmpty(temRoomVOList)) {
                continue;
            }

            // 对这一层的房间按评分排序
            if (param.getHighScore() == SpaceConstant.Switch.YES) {
                Set<String> roomIdToOrder = roomIdAndRoomVO.keySet();
                roomIdOrderByScoreDesc.retainAll(roomIdToOrder);
                roomIdToOrder.removeAll(roomIdOrderByScoreDesc);
                roomIdOrderByScoreDesc.addAll(roomIdToOrder); // 这一层，可见的房间，已经按评分从高到低排序好放在roomIdOrderByScoreDesc中

                temRoomVOList = new ArrayList<SpaceRoomVO>();
                for (String rid : roomIdOrderByScoreDesc) {
                    SpaceRoomVO vo = roomIdAndRoomVO.get(rid);
                    Double score = roomIdAndAvgscore.get(rid);
                    if (score != null) {
                        vo.setCommentLevel(new BigDecimal(score).setScale(1, RoundingMode.HALF_UP).doubleValue());
                    }
                    temRoomVOList.add(vo);
                }
            }


            // ‘楼层数据’复制到‘楼层VO’
            SpaceFloorVO floorVO = new SpaceFloorVO();
            floorVO.setFloorId(floor.getFloorId());
            floorVO.setFloorNum(floor.getFloorNum());
            floorVO.setFloorName(floor.getFloorName());
            floorVO.setRoomList(temRoomVOList); // ‘空间VO集合’放入‘楼层VO’
            temFloorVOList.add(floorVO); // ‘楼层VO’放入‘楼层集合VO’
        }

        return R.ok(temFloorVOList);
    }


    /**
     * 手机接口：查询单个空间
     */
    @Override
    public R<OneBOneFOneR> getOneRoom(String roomId) {
        OneBOneFOneR oneBOneFOneR = getBaseMapper().getOneBuildOneFloorOneRoom(roomId);
        return R.ok(oneBOneFOneR);
    }


    /**
     * 统计中心，综合楼和楼层联动下拉框，综合楼必须是在当前用户的管理范围内
     */
    @Override
    @Transactional
    public R<List<DropDownVO>> getStatisticDropDown() {
        Set<String> orgIdList = spaceUtil.getOrgIdList();
        List<SpaceBuild> list = ((SpaceBuildMapper) buildService.getBaseMapper()).getManyBuildManyFloor(orgIdList);

        List<DropDownVO> result = new LinkedList<DropDownVO>();
        for (SpaceBuild build : list) {
            DropDownVO bvo = new DropDownVO();
            bvo.setValue(build.getBuildId());
            bvo.setLabel(build.getBuildName());

            List<SpaceFloor> floorList = build.getFloorList();
            List<DropDownVO> flist = new ArrayList<DropDownVO>();
            flist.add(new DropDownVO("", "全部", new ArrayList<DropDownVO>()));
            for (SpaceFloor floor : floorList) {
                DropDownVO fvo = new DropDownVO();
                fvo.setValue(floor.getFloorId());
                fvo.setLabel(floor.getFloorName());
                flist.add(fvo);
            }

            bvo.setChildren(flist);
            result.add(bvo);
        }

        return R.ok(result);
    }

    @Override
    public R<String> editRoomStateBatch(EditRoomStateBatchVO param) {
        List<String> roomIdList = param.getRoomIdList();
        Integer roomState = param.getRoomState();

        if (roomState.intValue() != SpaceConstant.Switch.CLOSE && roomState.intValue() != SpaceConstant.Switch.OPEN) {
            throw BusinessException.of("房间状态错误！");
        }

        // 记录哪些房间需要取消订单
        List<String> idList = new ArrayList<String>();

        // 第一：修改每个房间下面的座位状态，顺便记录需要取消订单的房间id
        List<SpaceRoom> oldList = lambdaQuery().in(SpaceRoom::getRoomId, roomIdList).list();
        for (SpaceRoom oldRoom : oldList) {
            SpaceRoom newRoom = new SpaceRoom();
            newRoom.setRoomId(oldRoom.getRoomId());
            newRoom.setRoomState(roomState);

            // 根据新房间和老房间，批量修改座位的状态
            changeStateByNewRoomAndOldRoom(newRoom, oldRoom);

            if (oldRoom.getRoomState().intValue() == SpaceConstant.Switch.OPEN) {
                if (newRoom.getRoomState().intValue() == SpaceConstant.Switch.CLOSE) {
                    // 取消订单的房间id
                    idList.add(oldRoom.getBuildId());
                }
            }
        }


        // 第二：修改房间本身的状态
        LambdaUpdateChainWrapper<SpaceRoom> lambdaUpdate = lambdaUpdate();
        lambdaUpdate.set(SpaceRoom::getRoomState, roomState);
        lambdaUpdate.in(SpaceRoom::getRoomId, roomIdList);
        lambdaUpdate.update();


        // 第三：取消订单
        if (!CollectionUtils.isEmpty(idList)) {
            buildService.cancelOrder(idList, SpaceConstant.SpaceType.ROOM);
        }

        return R.ok();
    }

    @Override
    @Transactional
    public R<String> importRoomImage(ImportRoomImageVO param) {
        String roomId = param.getRoomId();
        String roomImage = param.getRoomImage();

        SpaceRoom room = getById(roomId);
        if (room == null) {
            throw BusinessException.of("获取空间信息失败！");
        }

        lambdaUpdate().set(SpaceRoom::getRoomImage, spaceUtil.deleteIpAndPort(roomImage)).eq(SpaceRoom::getRoomId, roomId).update();
        return R.ok();
    }

}
