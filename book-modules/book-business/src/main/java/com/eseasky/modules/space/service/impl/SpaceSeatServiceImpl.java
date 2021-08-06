package com.eseasky.modules.space.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.order.config.OrderConstant;
import com.eseasky.modules.order.vo.OrderRuleVO;
import com.eseasky.modules.space.config.SpaceConstant;
import com.eseasky.modules.space.config.SpaceUtil;
import com.eseasky.modules.space.entity.SpaceConf;
import com.eseasky.modules.space.entity.SpaceDesk;
import com.eseasky.modules.space.entity.SpaceGroup;
import com.eseasky.modules.space.entity.SpaceRoom;
import com.eseasky.modules.space.entity.SpaceSeat;
import com.eseasky.modules.space.mapper.SpaceDeskMapper;
import com.eseasky.modules.space.mapper.SpaceRoomMapper;
import com.eseasky.modules.space.mapper.SpaceSeatMapper;
import com.eseasky.modules.space.service.SpaceBuildService;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceDeskService;
import com.eseasky.modules.space.service.SpaceGroupService;
import com.eseasky.modules.space.service.SpaceRoomService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.vo.SpaceCanvasVO;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.SpaceGroupVO;
import com.eseasky.modules.space.vo.request.ConfirmOrderParam;
import com.eseasky.modules.space.vo.request.QueryMobileSeatParam;
import com.eseasky.modules.space.vo.request.QueryOrderSeatParam;
import com.eseasky.modules.space.vo.request.QuickOrderParam;
import com.eseasky.modules.space.vo.request.SaveSeatVO;
import com.eseasky.modules.space.vo.response.ConfIdVO;
import com.eseasky.modules.space.vo.response.ConfirmOrderVO;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.ParentConfAndState;
import com.eseasky.modules.space.vo.response.QueryMobileSeatListVO;
import com.eseasky.modules.space.vo.response.SeatInfoToOrder;
import com.eseasky.modules.space.vo.response.SeatAndGroupForOrder;
import com.eseasky.modules.space.vo.response.SeatInfoForQuickOrder;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 座位服务实现类
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Slf4j
@Service
public class SpaceSeatServiceImpl extends ServiceImpl<SpaceSeatMapper, SpaceSeat> implements SpaceSeatService {

    @Autowired
    private SpaceBuildService buildService;
    @Autowired
    private SpaceRoomService roomService;
    @Autowired
    private SpaceDeskService deskService;
    @Autowired
    private SpaceConfService confService;
    @Autowired
    private SpaceGroupService groupService;
    @Autowired
    private SpaceUtil spaceUtil;

    @Override
    @Transactional
    public R<String> addOrUpdateSeat(SpaceCanvasVO canvasVO) {
        OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(canvasVO.getRoomId());
        if (room == null) {
            log.error("SpaceSeatServiceImpl-addOrUpdateSeat，获取空间失败，canvasVO：{}", JSON.toJSONString(canvasVO));
            throw BusinessException.of("获取空间失败！");
        }

        // 获取座位的状态和配置id
        ParentConfAndState confAndState = spaceUtil.getParentProp(room);
        String confId = confAndState.getConfId();
        Integer parentConf = confAndState.getParentConf();
        Integer seatState = confAndState.getSeatState();
        Integer parentState = confAndState.getParentState();


        Map<String, String> groupIdAndConfId = new HashMap<>();
        Map<String, SpaceGroupVO> groupVO = groupService.groupIdAndSpaceGroupVO(canvasVO.getRoomId());
        if (MapUtils.isNotEmpty(groupVO)) {
            for (SpaceGroupVO vo : groupVO.values()) {
                groupIdAndConfId.put(vo.getGroupId(), vo.getConfId());
            }
        }


        List<SaveSeatVO> canvases = canvasVO.getCanvases();
        if (CollectionUtils.isEmpty(canvases)) {
            throw BusinessException.of("获取画布集合失败！");
        }

        // 数据库原座位数据
        List<SpaceSeat> seatToLog = lambdaQuery().eq(SpaceSeat::getRoomId, room.getRoomId()).list();
        Map<String, SpaceSeat> seatIdAndOldSeat = new HashMap<>();
        for (SpaceSeat spaceSeat : seatToLog) {
            seatIdAndOldSeat.put(spaceSeat.getSeatId(), spaceSeat);
        }

        // 数据库原墙、课桌数据
        List<SpaceDesk> deskToLog = deskService.lambdaQuery().eq(SpaceDesk::getRoomId, room.getRoomId()).list();
        Map<String, SpaceDesk> deskIdAndOldDesk = new HashMap<>();
        for (SpaceDesk spaceDesk : deskToLog) {
            deskIdAndOldDesk.put(spaceDesk.getDeskId(), spaceDesk);
        }


        // canvases 集合中的数据分为三种：墙、课桌、座位
        // 墙、课桌放入space_desk表，座位放入space_seat表

        // 座位的五种组合
        // open -> delete 逻辑删除
        // open -> forbid 禁用
        // forbid -> opend 启用
        // forbid -> delete 逻辑删除
        // add 新增

        List<SpaceDesk> deskToSave = new ArrayList<>();
        List<SpaceSeat> seatToSave = new ArrayList<>();
        List<SpaceSeat> seatToUpdate = new ArrayList<>();
        Set<String> seatToForbid = new HashSet<String>(); // 禁用座位数量
        int seatTotalCount = 0; // 房间所有座位
        Date now = new Date();
        for (SaveSeatVO vo : canvases) {
            Integer type = vo.getType();
            Integer seatStateToUse = seatState;
            Integer parentStateToUse = parentState;

            // 1座位。老数据拿出来放入新对象，保存时先删除全部老数据，再保存新数据。
            if (SpaceConstant.CanvasType.SEAT == type) {
                seatTotalCount++;

                String seatId = vo.getSeatId();
                Integer voState = vo.getState(); // 1空闲未预约状态;2预约使用中;3预约暂时中;4座位自身禁用;
                SpaceSeat oldSeat = seatIdAndOldSeat.remove(seatId);
                SpaceSeat newSeat = new SpaceSeat();
                if (oldSeat == null) {
                    // 组合【add】
                    // 前端传了一个座位对象，在数据库不存在，说明此座位是新增，新增voState只认1和4
                    if (voState == SpaceConstant.CanvasState.NONE_ORDER) {
                        // 保持父节点seatState，parentState的值不变
                    } else if (voState == SpaceConstant.CanvasState.SEAT_CLOSE) {
                        seatStateToUse = SpaceConstant.Switch.CLOSE; // 转为数据库的禁用状态，并覆盖父节点的状态
                        parentStateToUse = SpaceConstant.SpaceType.SEAT; // 座位级别的禁用
                        seatToForbid.add(seatId);
                    } else {
                        continue;
                    }

                    // 新增座位
                    newSeat.setSeatId(UUID.randomUUID().toString().replaceAll("-", "")); // 后端新增
                    newSeat.setBuildId(room.getBuildId()); // 后端新增
                    newSeat.setFloorId(room.getFloorId()); // 后端新增
                    newSeat.setRoomId(room.getRoomId()); // 后端新增
                    newSeat.setType(type); // 前端传值
                    newSeat.setX(vo.getX()); // 前端传值
                    newSeat.setY(vo.getY()); // 前端传值
                    newSeat.setWebId(vo.getWebId()); // 前端传值

                    newSeat.setSeatNum(vo.getSeatNum()); // TODO, 以后再确定
                    if (StringUtils.isNotBlank(vo.getSeatGroupId())) { // groupId不为空，就用分组的confId
                        newSeat.setSeatGroupId(vo.getSeatGroupId());
                        newSeat.setConfId(groupIdAndConfId.get(vo.getSeatGroupId()));
                        newSeat.setParentConf(SpaceConstant.SpaceType.GROUP);
                    } else {
                        newSeat.setSeatGroupId("");
                        newSeat.setConfId(confId); // 判断座位状态并可能继承父节点状态
                        newSeat.setParentConf(parentConf); // 判断座位状态并可能继承父节点状态
                    }

                    newSeat.setSeatState(seatStateToUse); // 判断座位状态并可能继承父节点状态
                    newSeat.setParentState(parentStateToUse); // 判断座位状态并可能继承父节点状态

                    newSeat.setCreateTime(now); // 后端新增
                    newSeat.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId()); // 后端新增

                    seatToSave.add(newSeat);
                } else {
                    // 前端的画布在数据库存在
                    // 组合【open -> forbid】【forbid -> open】
                    // 1空闲未预约状态;2预约使用中;3预约暂时中;4座位自身禁用;
                    if (voState == SpaceConstant.CanvasState.SEAT_CLOSE) { // 前端告诉后端此座位需要禁用
                        seatStateToUse = SpaceConstant.Switch.CLOSE; // 转为数据库的禁用状态，并覆盖父节点的状态
                        parentStateToUse = SpaceConstant.SpaceType.SEAT; // 座位级别的禁用
                        seatToForbid.add(seatId);
                    } else {
                        // 保持父节点seatState，parentState的值不变
                    }

                    oldSeat.setType(vo.getType()); // 前端传值
                    oldSeat.setX(vo.getX()); // 前端传值
                    oldSeat.setY(vo.getY()); // 前端传值
                    oldSeat.setWebId(vo.getWebId()); // 前端传值

                    oldSeat.setSeatNum(vo.getSeatNum()); // TODO, 以后再确定
                    if (StringUtils.isNotBlank(vo.getSeatGroupId())) { // groupId不为空，就用分组的confId
                        newSeat.setSeatGroupId(vo.getSeatGroupId());
                        newSeat.setConfId(groupIdAndConfId.get(vo.getSeatGroupId()));
                        newSeat.setParentConf(SpaceConstant.SpaceType.GROUP);
                    } else {
                        newSeat.setSeatGroupId("");
                        newSeat.setConfId(confId); // 判断座位状态并可能继承父节点状态
                        newSeat.setParentConf(parentConf); // 判断座位状态并可能继承父节点状态
                    }

                    oldSeat.setSeatState(seatStateToUse); // 判断座位状态并可能继承父节点状态
                    oldSeat.setParentState(parentStateToUse); // 判断座位状态并可能继承父节点状态

                    oldSeat.setUpdateUser(SecurityUtils.getUser().getSysUserDTO().getId());
                    oldSeat.setUpdateTime(now);

                    seatToUpdate.add(oldSeat);
                }
            }

            // 2课桌;3墙
            // 墙、课桌的两种组合
            // open -> delete 逻辑删除
            // add 新增
            else if (SpaceConstant.CanvasType.DESK == type || SpaceConstant.CanvasType.WALL == type) {
                String deskId = vo.getSeatId();
                SpaceDesk oldDesk = deskIdAndOldDesk.remove(deskId);
                SpaceDesk newDesk = new SpaceDesk();

                // 前端传了一个课桌墙对象，但在数据库不存在，说明此课桌墙是新增
                if (oldDesk == null) {
                    newDesk.setDeskId(UUID.randomUUID().toString().replaceAll("-", ""));
                    newDesk.setBuildId(room.getBuildId()); // 后端新增
                    newDesk.setFloorId(room.getFloorId()); // 后端新增
                    newDesk.setRoomId(room.getRoomId()); // 后端新增
                    newDesk.setType(type); // 前端传值
                    newDesk.setX(vo.getX()); // 前端传值
                    newDesk.setY(vo.getY()); // 前端传值
                    newDesk.setWebId(vo.getWebId()); // 前端传值
                    newDesk.setCanvasState(vo.getState()); // 前端传值
                    newDesk.setDeskState(SpaceConstant.Switch.YES); // 后端新增，此字段暂时用不到，先设置为可用状态1
                    newDesk.setCreateTime(now);
                    newDesk.setCreateUser(SecurityUtils.getUser().getSysUserDTO().getId());
                    deskToSave.add(newDesk);
                } else {
                    // SpaceDesk可变的值只有三个
                    if (needUpdateDesk(oldDesk, vo)) {
                        // 更新可能变化的值
                        deskService.lambdaUpdate() //
                                .set(SpaceDesk::getX, vo.getX()) //
                                .set(SpaceDesk::getY, vo.getY()) //
                                .set(SpaceDesk::getWebId, vo.getWebId()) //
                                .set(SpaceDesk::getUpdateTime, now) //
                                .set(SpaceDesk::getUpdateUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                                .eq(SpaceDesk::getDeskId, oldDesk.getDeskId()) //
                                .update();
                    }
                }
            } else {
                log.error("SpaceSeatServiceImpl-addOrUpdateSeat，错误的画布状态，type：{}", type);
            }
        }

        saveBatch(seatToSave);
        updateBatchById(seatToUpdate);
        deskService.saveBatch(deskToSave);

        // 更新space_room表的seat_count、seat_not_forbid_count
        roomService.modifySeatCountAndDeskCount(room.getRoomId(), seatTotalCount, seatTotalCount - seatToForbid.size());

        // 组合【open -> delete】【forbid -> delete】
        // seatIdAndOldSeat不为空，剩下的数据就是需要删除的座位
        if (seatIdAndOldSeat.size() > 0) {
            Set<String> seatIdToDelete = seatIdAndOldSeat.keySet();
            // 在数据库逻辑删除座位
            removeByIds(seatIdToDelete);

            // 告诉预约接口，这些seatId里面的订单需要禁用
            seatToForbid.addAll(seatIdToDelete);
        }

        // 逻辑删除课桌墙
        if (deskIdAndOldDesk.size() > 0) {
            deskService.removeByIds(deskIdAndOldDesk.keySet());
        }

        // 根据座位id取消订单
        if (!seatToForbid.isEmpty()) {
            buildService.cancelOrder(Lists.newArrayList(seatToForbid), SpaceConstant.SpaceType.SEAT);
        }
        return R.ok();
    }

    // 三个值有一个不同就返回true，需要更新
    private boolean needUpdateDesk(SpaceDesk oldDesk, SaveSeatVO voSeat) {
        if (differentInt(oldDesk.getX(), voSeat.getX())) {
            return true;
        }
        if (differentInt(oldDesk.getY(), voSeat.getY())) {
            return true;
        }
        if (differentStr(oldDesk.getWebId(), voSeat.getWebId())) {
            return true;
        }
        return false;
    }

    // 值不同，返回true
    private boolean differentInt(Integer oldInt, Integer voInt) {
        if (oldInt == null && voInt == null) {
            return false;
        } else if (oldInt != null && voInt != null && oldInt.intValue() == voInt.intValue()) {
            return false;
        }
        return true;
    }

    // 值不同，返回true
    private boolean differentStr(String oldStr, String voStr) {
        if (StringUtils.isBlank(oldStr) && StringUtils.isBlank(voStr)) {
            return false;
        } else if (oldStr != null && voStr != null && oldStr.trim().equals(voStr.trim())) {
            return false;
        }
        return true;
    }

    /**
     * 电脑端空间管理：座位布局列表
     */
    @Override
    @Transactional
    public R<List<SaveSeatVO>> getSpaceSeatList(JSONObject param) {
        String roomId = param.getString("roomId");
        SpaceRoom room = roomService.getById(roomId);
        if (room == null) {
            log.error("SpaceSeatServiceImpl-findSeatList，获取空间失败，roomId：{}", roomId);
            throw BusinessException.of("获取空间失败！");
        }

        // 查询所有的座位，没有直接返回
        List<SaveSeatVO> seatList = getBaseMapper().findListByRoomId(roomId);
        if (CollectionUtils.isEmpty(seatList)) {
            return R.ok(new ArrayList<>());
        }

        // 循环处理，明确各个状态的含义。
        // 从数据拿出来的state，0座位禁用，1座位可以用
        // 下面将0变为前端的5，表示画布的禁用。将1变为前端的1，表示画布的空闲可使用。
        for (SaveSeatVO vo : seatList) {
            Integer state = vo.getState();
            Integer parentState = vo.getParentState();

            // 座位自身禁用
            if (parentState == SpaceConstant.SpaceType.SEAT && state == SpaceConstant.Switch.CLOSE) {
                vo.setState(SpaceConstant.CanvasState.SEAT_CLOSE);
            } else {
                // 非座位禁用
                vo.setState(SpaceConstant.CanvasState.NONE_ORDER);
            }
        }

        // 查出课桌和墙的属性
        SpaceDeskMapper deskMapper = (SpaceDeskMapper) deskService.getBaseMapper();
        List<SaveSeatVO> deskList = deskMapper.findListByRoomId(roomId);
        seatList.addAll(deskList);

        return R.ok(seatList);
    }

    /**
     * 手机预约界面：座位选择列表
     */
    @Override
    @Transactional
    public R<QueryMobileSeatListVO<SaveSeatVO>> getMobileSeatList(QueryMobileSeatParam param) {
        Integer closeWindow = param.getCloseWindow();
        Integer haveSocket = param.getHaveSocket();
        Integer awayToilet = param.getAwayToilet();
        Integer awayDoor = param.getAwayDoor();
        String roomId = param.getRoomId();
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
            log.error("SpaceBuildServiceImpl-getMobileRoomList，日期格式错误，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("日期格式错误！");
        }


        /** 规则筛选 */
        ConfIdVO confIdVO = spaceUtil.getConfIdListForCurrentUser(startTime, endTime, startDate, endDate, orderType);
        List<String> freeList = confIdVO.getFreeList();


        /** 获取房间信息，用于前端展示 */
        SpaceRoomMapper roomMapper = (SpaceRoomMapper) roomService.getBaseMapper();
        OneBOneFOneR oneBOneFOneR = roomMapper.getOneBuildOneFloorOneRoom(roomId);
        if (oneBOneFOneR == null) {
            log.error("SpaceSeatServiceImpl-getMobileSeatList，获取空间信息失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取空间信息失败！");
        }


        /** 单人长租和短租，只用于座位。可预约confId、可见seat、非删除seat、指定roomId，排除时间段内占用的seatId */
        List<String> notForbidSeatIdList = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(freeList)) {
            notForbidSeatIdList = getBaseMapper().singleOrderForSeat(freeList, roomId, startDate, endDate);
        }


        /** 手机端勾选了座位属性 */
        List<String> seatIdbyTag = spaceUtil.filterSeatIdByTag(roomId, closeWindow, haveSocket, awayToilet, awayDoor);
        if (seatIdbyTag == null) {
            // 手机端未勾选任何过滤条件
        } else if (seatIdbyTag.size() == 0) {
            // 手机端勾选了过滤条件，但是没查到数据
            notForbidSeatIdList.clear();
        } else {
            // 手机端勾选了过滤条件，并且查到数据，取交集。
            notForbidSeatIdList.retainAll(seatIdbyTag);
        }


        // 查询此空间的所有的座位
        List<SaveSeatVO> seatList = getBaseMapper().findListByRoomId(roomId);


        // 座位自身状态：0座位禁用，1座位可以用
        // 前端画布状态：1空闲未预约状态,2预约使用中,3预约暂时中,4座位自身已禁用
        for (SaveSeatVO vo : seatList) {
            if (notForbidSeatIdList.contains(vo.getSeatId())) {
                vo.setState(SpaceConstant.CanvasState.NONE_ORDER);
            } else {
                // 将座位的禁用改为画布的禁用
                vo.setState(SpaceConstant.CanvasState.SEAT_CLOSE);
            }
        }

        // 查出课桌和墙的属性，和座位合并，返回前端
        List<SaveSeatVO> deskList = ((SpaceDeskMapper) deskService.getBaseMapper()).findListByRoomId(roomId);
        seatList.addAll(deskList);

        // 其他属性赋值
        QueryMobileSeatListVO<SaveSeatVO> result = new QueryMobileSeatListVO<SaveSeatVO>();
        result.setBuildName(oneBOneFOneR.getBuildName());
        result.setBuildNum(oneBOneFOneR.getBuildNum());
        result.setFloorName(oneBOneFOneR.getFloorName());
        result.setFloorNum(oneBOneFOneR.getFloorNum());
        result.setRoomName(oneBOneFOneR.getRoomName());
        result.setRoomNum(oneBOneFOneR.getRoomNum());
        result.setSeatCount(oneBOneFOneR.getSeatCount());
        result.setSeatNotUsedCount(notForbidSeatIdList.size());
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        result.setList(seatList);
        return R.ok(result);
    }

    /**
     * 统计中心：座位展示列表
     */
    @Override
    @Transactional
    public R<QueryMobileSeatListVO<SaveSeatVO>> getStatisticSeatList(JSONObject param) {
        String roomId = param.getString("roomId");

        // 先获取空间信息
        SpaceRoomMapper roomMapper = (SpaceRoomMapper) roomService.getBaseMapper();
        OneBOneFOneR oneBOneFOneR = roomMapper.getOneBuildOneFloorOneRoom(roomId);
        if (oneBOneFOneR == null) {
            log.error("SpaceSeatServiceImpl-getOrderSeatList，获取空间信息失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取空间信息失败！");
        }

        // 根据空间ids查询当前时间点的座位占用数量
        List<JSONObject> list = getBaseMapper().getInUsedSeatForStatistic(new Date(), roomId);
        JSONObject orderedSeat = new JSONObject();
        HashSet<String> groupedSeat = new HashSet<>();
        for (JSONObject map : list) {
            String seatId = map.getString("seatId");
            String seatGroupId = map.getString("seatGroupId");
            Integer listState = map.getInteger("listState");

            // 单人短租、长租
            if (StringUtils.isNotBlank(seatId) && listState != null) {
                if (listState == OrderConstant.listState.IN_USE || listState == OrderConstant.listState.AWAY) {
                    orderedSeat.put(map.getString("seatId"), listState);
                }
            }

            // 拼团
            if (StringUtils.isNotBlank(seatGroupId)) {
                groupedSeat.add(seatGroupId);
            }
        }

        // 座位自身状态：0座位禁用，1座位可以用
        // 前端画布状态：1空闲未预约状态,2预约使用中,3预约暂时中,4座位自身已禁用
        // 订单状态：1待签到;2使用中;3暂离（使用中）;4已完成;5未签到（违约）;6未签退（违约）;7暂离未返回（违约）;8待签退
        List<SaveSeatVO> seatList = getBaseMapper().findListByRoomId(roomId);
        for (SaveSeatVO vo : seatList) {
            Integer seatState = vo.getState(); // 座位自身状态：0座位禁用（来自综合楼、房间、座位），1座位可以用
            if (seatState == null || seatState == SpaceConstant.Switch.CLOSE) {
                vo.setState(SpaceConstant.CanvasState.SEAT_CLOSE); // 将座位的禁用改为画布的禁用
                continue;
            }

            int orderState = orderedSeat.getIntValue(vo.getSeatId()); // 只有两种订单状态：2使用中;3暂离
            if (orderState == OrderConstant.listState.AWAY) {
                vo.setState(SpaceConstant.CanvasState.LEAVE_ORDER); // 根据时间段查询此座位在订单中的状态是3暂离，对画布就是座位暂时中
                continue;
            } else if (orderState == OrderConstant.listState.IN_USE) {
                vo.setState(SpaceConstant.CanvasState.ALREADY_ORDER); // 根据时间段查询此座位在订单中的状态是1待签到|2使用中，对画布就2预约使用中
                continue;
            } else {
                if (groupedSeat.contains(vo.getSeatGroupId())) { // 判断此座位是否是正在使用的拼团
                    vo.setState(SpaceConstant.CanvasState.ALREADY_ORDER);
                    continue;
                }
            }

            // 剩下的都是画布可预约状态
            vo.setState(SpaceConstant.CanvasState.NONE_ORDER);
        }

        // 查出课桌和墙的属性，和座位合并，返回前端
        List<SaveSeatVO> deskList = ((SpaceDeskMapper) deskService.getBaseMapper()).findListByRoomId(roomId);
        seatList.addAll(deskList);

        // 其他属性赋值
        QueryMobileSeatListVO<SaveSeatVO> result = new QueryMobileSeatListVO<SaveSeatVO>();
        result.setBuildName(oneBOneFOneR.getBuildName());
        result.setBuildNum(oneBOneFOneR.getBuildNum());
        result.setFloorName(oneBOneFOneR.getFloorName());
        result.setFloorNum(oneBOneFOneR.getFloorNum());
        result.setRoomName(oneBOneFOneR.getRoomName());
        result.setRoomNum(oneBOneFOneR.getRoomNum());
        result.setSeatCount(oneBOneFOneR.getSeatCount());
        result.setList(seatList);
        return new R<QueryMobileSeatListVO<SaveSeatVO>>(result);
    }

    /**
     * 快速预约
     */
    @Override
    @Transactional
    public R<SeatInfoForQuickOrder> mobileQuickOrder(QuickOrderParam param) {
        String buildId = param.getBuildId();
        Integer orderType = param.getOrderType(); // 1短租 2长租
        String sDate = param.getStartDate();
        String eDate = param.getEndDate();
        String startTime = param.getStartTime(); // HH:mm
        String endTime = param.getEndTime(); // HH:mm，非手机端参数，下面判断用到

        if (orderType.intValue() != SpaceConstant.OrderType.SINGLE_ONCE) {
            throw BusinessException.of("请选择单次预约！");
        }

        Date startDate = null;
        Date endDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            startDate = sdf.parse(sDate + " " + startTime);
            endDate = sdf.parse(eDate + " " + endTime);
        } catch (Exception e) {
            log.error("SpaceBuildServiceImpl-mobileQuickOrder，日期格式错误，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("日期格式错误！");
        }


        // 获取当前用户可见的confIdList
        ConfIdVO confIdVO = spaceUtil.getConfIdListForCurrentUser(startTime, endTime, startDate, endDate, orderType);
        if (CollectionUtils.isEmpty(confIdVO.getFreeList())) {
            throw BusinessException.of("暂无适合您的场馆！");
        }

        String seatId = getBaseMapper().getSeatIdForQuickOrder(confIdVO.getFreeList(), buildId, startDate, endDate);
        if (StringUtils.isBlank(seatId)) {
            throw BusinessException.of("暂无适合您的场馆！");
        }

        SeatInfoForQuickOrder seat = getBaseMapper().getSeatInfoForQuickOrder(seatId);
        seat.setOrderType(param.getOrderType());
        seat.setStartDate(param.getStartDate());
        seat.setEndDate(param.getEndDate());
        seat.setStartTime(param.getStartTime());
        seat.setEndTime(param.getEndTime());
        return R.ok(seat);
    }


    /**
     * 为订单获取座位信息和规则信息
     */
    @Override
    @Transactional
    public R<SeatInfoToOrder> getSeatInfoToOrder(QueryOrderSeatParam param) {
        Date startDate = param.getStartDate();
        Date endDate = param.getEndDate();
        Integer orderType = param.getOrderType();
        String seatId = param.getSeatId();
        String groupId = param.getGroupId();

        if (orderType == null) {
            return aa("缺少预约类型！");
        }
        if (startDate == null || endDate == null) {
            return aa("缺少预约时间！");
        }

        // 短租需要判断开放时间
        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE) {
            if (startDate.getTime() < new Date().getTime()) {
                return aa("预约时间不得小于当前时间！");
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String startTime = sdf.format(startDate);
        String endTime = sdf.format(endDate);


        SeatInfoToOrder order = null;
        if (StringUtils.isNotBlank(seatId)) {
            order = getBaseMapper().getSeatInfoForOrder(seatId);
            if (order == null) {
                return aa("获取座位失败！");
            }
        } else if (StringUtils.isNotBlank(groupId)) {
            order = getBaseMapper().getGroupInfoForOrder(groupId);
            if (order == null) {
                return aa("获取座位组失败！");
            }
        } else {
            return aa("请填写座位或座位组信息！");
        }


        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null) {
            return aa("获取配置信息失败！");
        }
        SpaceConfVO conf = confIdAndSpaceConf.get(order.getConfId());
        if (conf == null) {
            return aa("获取配置信息失败！");
        }


        // 首先判断是否是组团（5），如果是组团并且配置也开启了组团，就将orderType改为1
        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_GROUP) {
            if (conf.getIsGroup().intValue() == SpaceConstant.Switch.YES) {
                orderType = SpaceConstant.OrderType.SINGLE_ONCE; // 组团也是单人单次预约
            } else {
                // 手机端查询是组团（5），但是该配置不支持组团，直接跳过
                return aa("此座位组未开启拼团！");
            }
        }


        // 短租需要判断开放时间
        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE) {
            String judgeConf = spaceUtil.judgeOpenTime(conf, startTime, endTime, startDate, endDate, orderType);
            if (judgeConf != null) {
                return aa(judgeConf);
            }
        }

        // 长租和短租都要判断开放对象
        String showRoom = spaceUtil.showRoom(conf, orderType);
        if (showRoom != null) {
            return aa(showRoom);
        }

        // 将规则属性复制到SeatInfoForOrder
        BeanUtils.copyProperties(conf, order);
        order.setApproveList(conf.getApproveList().stream().map(m -> m.getUserId()).collect(Collectors.toList()));
        return R.ok(order);
    }


    /**
     * 为订单获取座位信息和规则信息
     */
    @Override
    public R<SeatAndGroupForOrder> getSeatAndGroupForOrder(String seatId, String seatGroupId) {
        SeatAndGroupForOrder result = new SeatAndGroupForOrder();

        String confId = null;
        if (StringUtils.isNotBlank(seatId)) {
            SpaceSeat seat = lambdaQuery().select(SpaceSeat::getConfId, SpaceSeat::getSeatId).eq(SpaceSeat::getSeatId, seatId).one();
            if (seat == null) {
                return bb("获取座位失败！");
            }
            result.setSeatNum(seat.getSeatNum());
            confId = seat.getConfId();
        } else if (StringUtils.isNotBlank(seatGroupId)) {
            SpaceGroup group = groupService.getById(seatGroupId);
            if (group == null) {
                return bb("获取座位分组失败！");
            }
            result.setGroupName(group.getGroupName());
            confId = group.getConfId();
        }

        if (confId == null) {
            return bb("获取规则失败！");
        }

        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null) {
            return bb("获取配置失败！");
        }
        SpaceConfVO vo = confIdAndSpaceConf.get(confId);
        if (vo == null) {
            return bb("获取配置规则失败！");
        }
        if (CollectionUtils.isEmpty(vo.getSignList())) {
            return bb("获取签到方式失败！");
        }
        result.setSignId(vo.getSignList().get(0).getSignId());

        return R.ok(result);
    }


    @Override
    @Transactional
    public R<String> modifyConfId(JSONObject param) {
        String seatId = param.getString("seatId");
        String confId = param.getString("confId");
        if (StringUtils.isBlank(seatId) || StringUtils.isBlank(confId)) {
            log.error("SpaceSeatServiceImpl-modifyConfId，获取参数失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取参数失败！");
        }


        // 判断是否是座位组，被绑定的不能修改
        SpaceSeat seat = getById(seatId);
        if (seat == null) {
            log.error("SpaceSeatServiceImpl-modifyConfId，获取座位失败，param：{}", JSON.toJSONString(param));
            throw BusinessException.of("获取座位失败！");
        }
        if (StringUtils.isNotBlank(seat.getSeatGroupId())) {
            throw BusinessException.of("此座位已和其他座位绑定，不能单独修改！");
        }


        // 判断此座位原来绑定的规则是否是会议室模式
        SpaceConf oldConf = confService.lambdaQuery().eq(SpaceConf::getConfId, seat.getConfId()).one();
        if (oldConf == null) {
            throw BusinessException.of("获取配置规则失败！");
        }
        // 此座位所在空间是多人的会议室模式就不能修改，会议室模式只能已房间为单位进行修改
        if (oldConf.getOrderType().intValue() == SpaceConstant.OrderType.MULTI_ONCE
                || oldConf.getOrderType().intValue() == SpaceConstant.OrderType.MULTI_LONG) {
            throw BusinessException.of("已被绑定为" + SpaceConstant.OrderTypeName.MAP.get(oldConf.getOrderType().intValue()) + "的座位不能单独修改！");
        }


        // 多人长租和多人短租只能配置在综合楼和房间
        SpaceConf newConf = confService.lambdaQuery().eq(SpaceConf::getConfId, confId).one();
        if (newConf == null) {
            throw BusinessException.of("获取配置规则失败！");
        }
        if (newConf.getOrderType().intValue() == SpaceConstant.OrderType.MULTI_ONCE
                || newConf.getOrderType().intValue() == SpaceConstant.OrderType.MULTI_LONG) {
            throw BusinessException.of("多人预约规则不能配置到座位！");
        }


        // 更新座位配置
        lambdaUpdate().set(SpaceSeat::getConfId, confId) //
                .set(SpaceSeat::getConfUser, SecurityUtils.getUser().getSysUserDTO().getId()) //
                .set(SpaceSeat::getConfTime, new Date()) //
                .eq(SpaceSeat::getSeatId, seatId) //
                .update();

        return R.ok("修改成功！");
    }

    /**
     * 根据座位id获取预约规则和签到规则
     */
    @Override
    public R<OrderRuleVO> getOrderRule(String seatId, String seatGroupId) {
        String confId = null;
        if (StringUtils.isNotBlank(seatId)) {
            SpaceSeat seat = lambdaQuery().select(SpaceSeat::getConfId, SpaceSeat::getSeatId).eq(SpaceSeat::getSeatId, seatId).one();
            if (seat == null) {
                return cc("获取座位失败！");
            }
            confId = seat.getConfId();
        } else if (StringUtils.isNotBlank(seatGroupId)) {
            SpaceGroup group = groupService.getById(seatGroupId);
            if (group == null) {
                return cc("获取座位分组失败！");
            }
            confId = group.getConfId();
        }

        if (confId == null) {
            return cc("获取规则失败！");
        }

        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null) {
            return cc("获取配置失败！");
        }
        SpaceConfVO vo = confIdAndSpaceConf.get(confId);
        if (vo == null) {
            return cc("获取配置规则失败！");
        }

        OrderRuleVO result = new OrderRuleVO();
        BeanUtils.copyProperties(vo, result);
        return R.ok(result);
    }


    private R<SeatInfoToOrder> aa(String msg) {
        return new R<SeatInfoToOrder>(R.FAIL_CODE, msg, null);
    }

    private R<SeatAndGroupForOrder> bb(String msg) {
        return new R<SeatAndGroupForOrder>(R.FAIL_CODE, msg, null);
    }

    private R<OrderRuleVO> cc(String msg) {
        return new R<OrderRuleVO>(R.FAIL_CODE, msg, null);
    }


    /**
     * 手机预约界面：确认订单时查询座位|座位组|房间详情。此方法不需要判断各种条件。
     */
    @Override
    @Transactional
    public R<ConfirmOrderVO> getInfoToConfirmOrder(ConfirmOrderParam param) {
        Integer orderType = param.getOrderType();
        String spaceId = param.getSpaceId();

        ConfirmOrderVO vo = new ConfirmOrderVO();
        vo.setOrderType(orderType);
        if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_ONCE || orderType.intValue() == SpaceConstant.OrderType.SINGLE_LONG) {
            SpaceSeat seat = getById(spaceId);
            if (seat == null) {
                log.error("SpaceSeatServiceImpl-getInfoToConfirmOrder，获取座位失败，param：{}", JSON.toJSONString(param));
                throw BusinessException.of("获取座位失败！");
            }
            vo.setSeatId(seat.getSeatId());
            vo.setSeatNum(seat.getSeatNum());
            OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(seat.getRoomId());
            if (room == null) {
                log.error("SpaceSeatServiceImpl-getInfoToConfirmOrder，获取空间失败，param：{}", JSON.toJSONString(param));
                throw BusinessException.of("获取空间失败！");
            }
            BeanUtils.copyProperties(room, vo);
        } else if (orderType.intValue() == SpaceConstant.OrderType.SINGLE_GROUP) {
            SpaceGroup group = groupService.getById(spaceId);
            if (group == null) {
                log.error("SpaceSeatServiceImpl-getInfoToConfirmOrder，获取座位组失败，param：{}", JSON.toJSONString(param));
                throw BusinessException.of("获取座位组失败！");
            }
            vo.setGroupId(spaceId);
            vo.setGroupName(group.getGroupName());
            List<Object> selectObjs = getBaseMapper().selectObjs(new QueryWrapper<SpaceSeat>().lambda() //
                    .select(SpaceSeat::getSeatNum) //
                    .eq(SpaceSeat::getRoomId, group.getRoomId()) //
                    .eq(SpaceSeat::getSeatState, SpaceConstant.Switch.OPEN) //
                    .eq(SpaceSeat::getSeatGroupId, spaceId));
            if (CollectionUtils.isEmpty(selectObjs)) {
                log.error("SpaceSeatServiceImpl-getInfoToConfirmOrder，获取座位组座位编号失败，param：{}", JSON.toJSONString(param));
                throw BusinessException.of("获取座位组座位编号失败！");
            }
            List<String> seatNumList = new ArrayList<String>();
            for (Object object : selectObjs) {
                if (object == null) {
                    seatNumList.add("");
                } else {
                    seatNumList.add(object.toString());
                }
            }
            vo.setSeatNumList(seatNumList);
            OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(group.getRoomId());
            if (room == null) {
                log.error("SpaceSeatServiceImpl-getInfoToConfirmOrder，获取空间失败，param：{}", JSON.toJSONString(param));
                throw BusinessException.of("获取空间失败！");
            }
            BeanUtils.copyProperties(room, vo);
        } else if (orderType.intValue() == SpaceConstant.OrderType.MULTI_ONCE || orderType.intValue() == SpaceConstant.OrderType.MULTI_LONG) {
            OneBOneFOneR room = ((SpaceRoomMapper) roomService.getBaseMapper()).getOneBuildOneFloorOneRoom(spaceId);
            if (room == null) {
                log.error("SpaceSeatServiceImpl-getInfoToConfirmOrder，获取空间失败，param：{}", JSON.toJSONString(param));
                throw BusinessException.of("获取空间失败！");
            }
            BeanUtils.copyProperties(room, vo);
        }

        return R.ok(vo);
    }
}
