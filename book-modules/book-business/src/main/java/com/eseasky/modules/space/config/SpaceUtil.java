package com.eseasky.modules.space.config;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.eseasky.common.code.dto.OrgDTO;
import com.eseasky.common.code.utils.BusinessException;
import com.eseasky.common.code.utils.SecurityUtils;
import com.eseasky.modules.space.entity.SpaceTag;
import com.eseasky.modules.space.mapper.SpaceSeatMapper;
import com.eseasky.modules.space.service.SpaceConfService;
import com.eseasky.modules.space.service.SpaceSeatService;
import com.eseasky.modules.space.service.SpaceTagService;
import com.eseasky.modules.space.vo.SpaceConfDateVO;
import com.eseasky.modules.space.vo.SpaceConfDeptVO;
import com.eseasky.modules.space.vo.SpaceConfDutyVO;
import com.eseasky.modules.space.vo.SpaceConfTimeVO;
import com.eseasky.modules.space.vo.SpaceConfUserVO;
import com.eseasky.modules.space.vo.SpaceConfVO;
import com.eseasky.modules.space.vo.SpaceConfWeekVO;
import com.eseasky.modules.space.vo.response.ConfIdVO;
import com.eseasky.modules.space.vo.response.OneBOneF;
import com.eseasky.modules.space.vo.response.OneBOneFOneR;
import com.eseasky.modules.space.vo.response.ParentConfAndState;

@Component
public class SpaceUtil {

    public String prefix = "/static";

    @Autowired
    private SpaceSeatService seatService;
    @Autowired
    private SpaceConfService confService;
    @Autowired
    private SpaceTagService tagService;

    public String deleteIpAndPort(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        int index = path.indexOf(prefix);
        if (index > 0) {
            return path.substring(index);
        }
        return path;
    }

    public String addIpAndPort(String path, String ipAndPort) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        if (path.startsWith(prefix)) {
            return ipAndPort + path;
        }
        return path;
    }

    // 判断职务id，学历id，可预约部门id
    public String showRoom(SpaceConfVO conf, Integer orderType) {

        List<String> orgIds = getDeptIdList();
        String userType = getUserType();
        String userId = getUserId();

        /************** 校验预约类型是否一致 **************/
        Integer conf_orderType = conf.getOrderType();
        if (conf_orderType == null || conf_orderType.intValue() != orderType.intValue()) {
            return "预约类型与配置规则不符！";
        }


        /************** 校验是否指定了人员id，如果指定了人员就不要判断部门和职务，直接返回null **************/
        List<SpaceConfUserVO> userList = conf.getUserList();
        if (!CollectionUtils.isEmpty(userList)) {
            for (SpaceConfUserVO spaceConfUser : userList) {
                if (spaceConfUser == null) {
                    continue;
                }
                if (userId.equals(spaceConfUser.getUserId())) {
                    return null;
                }
            }
        }


        /************** 校验部门id是否一致 **************/
        List<SpaceConfDeptVO> deptList = conf.getDeptList();
        boolean conform = false; // 不展示
        if (CollectionUtils.isEmpty(deptList)) {
            conform = true; // 未配置部门，所有部门都可预约。一个角色的管理部门可以为空；内置规则的开放部门都是空。
        } else {
            // 配置了部门，就必须包含当前用户的部门
            for (SpaceConfDeptVO spaceConfDept : deptList) {
                if (spaceConfDept == null) {
                    continue;
                }
                if (orgIds.contains(spaceConfDept.getDeptId())) {
                    conform = true; // 展示
                    break;
                }
            }
        }
        if (!conform) {
            return "部门规则与当前用户不符！";
        }

        /************** 校验职务id，dutyList为空就默认所有类型的人员可用 **************/
        List<SpaceConfDutyVO> dutyList = conf.getDutyList();
        conform = false; // 不展示
        if (CollectionUtils.isEmpty(dutyList)) {
            conform = true; // 展示
        } else {
            for (SpaceConfDutyVO duty : dutyList) {
                if (duty == null) {
                    continue;
                }
                if (userType.equals(duty.getDutyId())) { // 用户职务等于配置职务
                    conform = true; // 展示
                    break;
                }
            }
        }
        if (!conform) {
            return "职务规则与当前用户不符！";
        }

        // 所有校验通过，直接返回null
        return null;
    }


    /** 预约时间段是否在配置规则的允许时间段内 */
    @SuppressWarnings("deprecation")
    public String judgeOpenTime(SpaceConfVO conf, String startTime, String endTime, Date startDate, Date endDate, Integer orderType) {

        /************** 预约类型 **************/
        /** 用户点击查询，先判断所选日期时间是否符合配置规则，不符合就返回字符串 */
        Integer conf_orderType = conf.getOrderType();
        if (conf_orderType.intValue() != orderType.intValue()) {
            return "预约类型不符合！";
        }

        /************** 可预约日期段，dateList为null默认全年可以 **************/
        List<SpaceConfDateVO> dateList = conf.getDateList();
        if (!CollectionUtils.isEmpty(dateList)) {
            String msg = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (SpaceConfDateVO refuse : dateList) {
                if (refuse == null) {
                    continue;
                }
                Date allowStart = refuse.getAllowStartDate(); // 'yyyy-MM-dd 00:00:00'
                Date allowEnd = refuse.getAllowEndDate(); // 'yyyy-MM-dd 23:59:59'
                if (startDate.getTime() >= allowStart.getTime() && startDate.getTime() < allowEnd.getTime()) {
                    if (endDate.getTime() > allowStart.getTime() && endDate.getTime() <= allowEnd.getTime()) {
                        msg = null;
                        break;
                    } else {
                        msg = sdf.format(endDate) + "不可预约！";
                    }
                } else {
                    msg = sdf.format(startDate) + "不可预约！";
                }
            }
            if (msg != null) {
                return msg;
            }
        }

        /************** 预约周，weekList为null默认全周可以 **************/
        List<SpaceConfWeekVO> weekList = conf.getWeekList();
        if (!CollectionUtils.isEmpty(weekList)) {
            int today = startDate.getDay();
            final int toFilter = today == 0 ? 7 : today; // [1,2,3,4,5,6,0] ==> [1,2,3,4,5,6,7]
            List<SpaceConfWeekVO> collect = weekList.stream().filter(m -> m != null && (toFilter + "").equals(m.getWeekId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                return SpaceConstant.Week.MAP.get(toFilter) + "不可预约！";
            }
        }


        /************** 可预约时间段，timeList为null默认全天可以 **************/
        List<SpaceConfTimeVO> timeList = conf.getTimeList();
        if (!CollectionUtils.isEmpty(timeList)) {
            String msg = null;
            for (SpaceConfTimeVO allow : timeList) {
                if (allow == null) {
                    continue;
                }
                String allowStartTime = allow.getAllowStartTime().trim();
                String allowEndTime = allow.getAllowEndTime().trim();
                if (startTime.compareTo(allowStartTime) >= 0 && startTime.compareTo(allowEndTime) <= 0) {
                    if (endTime.compareTo(allowStartTime) >= 0 && endTime.compareTo(allowEndTime) <= 0) {
                        msg = null;
                        break;
                    } else {
                        msg = endTime + "不可预约！";
                    }
                } else {
                    msg = startTime + "不可预约！";
                }
            }
            if (msg != null) {
                return msg;
            }
        }

        // 判断提前预约天数
        Integer advanceDay = conf.getSubAdvanceDay();
        if (advanceDay != null) {
            // 今天最大时间
            LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
            // 提前天数的最大时间
            LocalDateTime plusDays = today.plusDays(advanceDay.intValue());
            Timestamp timestamp = Timestamp.valueOf(plusDays);

            if (advanceDay.intValue() == 0) {
                if (endDate.getTime() > timestamp.getTime()) {
                    return "只能当天预约！";
                }
            } else {
                if (endDate.getTime() > timestamp.getTime()) {
                    return "只能提前" + advanceDay.intValue() + "天预约！";
                }
            }

        }

        return null;
    }


    /** 此方法适用于后台实时空间数据中的空间列表展示，根据空间ids查询当前时间点的座位占用数量 */
    public Map<String, Integer> roomIdAndSeatInUsedCount(Date startDate, Collection<String> roomIdList) {
        List<JSONObject> list = ((SpaceSeatMapper) seatService.getBaseMapper()).getOrderedSeatByRoomIds(startDate, roomIdList);

        Map<String, Integer> result = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        for (JSONObject map : list) {
            result.put(map.getString("roomId"), map.getIntValue("seatInUsedCount"));
        }
        return result;
    }

    /** 此方法适用于后台实时空间数据中的空间列表展示，根据空间ids，判断这些空间在时间点是否被占用 */
    public List<String> getMeetingRoomInUsedList(Date startDate, Collection<String> roomIdList) {
        return ((SpaceSeatMapper) seatService.getBaseMapper()).getMeetingRoomInUsedList(startDate, roomIdList);
    }

    private String getUserId() {
        return SecurityUtils.getUser().getSysUserDTO().getId();
    }

    private String getUserType() {
        String userType = SecurityUtils.getUser().getSysUserDTO().getUserType();
        if (StringUtils.isBlank(userType)) {
            throw BusinessException.of("用户类型异常！");
        }
        return userType;
    }

    private List<String> getDeptIdList() {
        List<OrgDTO> sysOrgs = SecurityUtils.getUser().getSysUserDTO().getSysOrgs();
        if (CollectionUtils.isEmpty(sysOrgs)) {
            throw BusinessException.of("您没有可预约的空间！");
        }

        List<String> orgIdList = sysOrgs.stream().map(m -> m.getId()).collect(Collectors.toList());
        return orgIdList;
    }


    public ConfIdVO getConfIdListForCurrentUser(String startTime, String endTime, Date startDate, Date endDate, Integer orderType) {
        Map<String, SpaceConfVO> confIdAndSpaceConf = confService.confIdAndSpaceConf(SecurityUtils.getUser().getTenantCode());
        if (confIdAndSpaceConf == null || confIdAndSpaceConf.isEmpty()) {
            return null;
        }
        Collection<SpaceConfVO> allConfList = confIdAndSpaceConf.values();

        List<String> openList = new ArrayList<String>(); // 可见
        List<String> freeList = new ArrayList<String>(); // 可预约
        for (SpaceConfVO conf : allConfList) {

            int orderTypeToUse = orderType;

            // 前端传的参数是5，拼团
            if (orderTypeToUse == SpaceConstant.OrderType.SINGLE_GROUP) {

                // 如果是拼团，配置规则必须是单人短租
                if (conf.getOrderType().intValue() != SpaceConstant.OrderType.SINGLE_ONCE) {
                    continue;
                }

                // 是否开启拼团
                if (conf.getIsGroup().intValue() != SpaceConstant.Switch.YES) {
                    // 没开启跳过
                    continue;
                } else {
                    // 开启就改为单人短租
                    orderTypeToUse = SpaceConstant.OrderType.SINGLE_ONCE;
                }
            }
            // 前端传的参数是1234，绝对不能开启拼团
            else {
                if (conf.getIsGroup().intValue() == SpaceConstant.Switch.YES) {
                    continue;
                }
            }

            // 预约类型和规则不符合直接跳过
            if (conf.getOrderType().intValue() != orderTypeToUse) {
                continue;
            }


            // 判断开放对象规则是否符合当前用户
            String showRoom = showRoom(conf, orderTypeToUse);
            if (showRoom != null) {
                continue;
            }
            openList.add(conf.getConfId());


            // 长租没有开放时间
            if (orderTypeToUse == SpaceConstant.OrderType.SINGLE_ONCE || orderTypeToUse == SpaceConstant.OrderType.MULTI_ONCE) {
                String judgeOpenTime = judgeOpenTime(conf, startTime, endTime, startDate, endDate, orderTypeToUse);
                if (judgeOpenTime == null) {
                    // 短租的开放时间符合预约时间
                    freeList.add(conf.getConfId());
                }
            } else {
                // 长租不用判断开放时间，直接加入集合，和openList保持一致
                freeList.add(conf.getConfId());
            }
        }

        ConfIdVO result = new ConfIdVO();
        result.setOpenList(openList);
        result.setFreeList(freeList);
        return result;
    }


    public ParentConfAndState getParentProp(OneBOneFOneR room) {
        ParentConfAndState result = new ParentConfAndState();

        // 获取父节点的配置
        if (StringUtils.isNotBlank(room.getRoomConfId())) {
            result.setConfId(room.getRoomConfId());
            result.setParentConf(SpaceConstant.SpaceType.ROOM);
        } else if (StringUtils.isNotBlank(room.getFloorConfId())) {
            result.setConfId(room.getFloorConfId());
            result.setParentConf(SpaceConstant.SpaceType.FLOOR);
        } else if (StringUtils.isNotBlank(room.getBuildConfId())) {
            result.setConfId(room.getBuildConfId());
            result.setParentConf(SpaceConstant.SpaceType.BUILD);
        } else {
            result.setConfId(null);
            result.setParentConf(SpaceConstant.SpaceType.NONE);
        }

        // 获取父节点的禁用状态
        if (room.getRoomState() == SpaceConstant.Switch.CLOSE) {
            result.setSeatState(SpaceConstant.Switch.CLOSE);
            result.setParentState(SpaceConstant.SpaceType.ROOM);
        } else if (room.getBuildState() == SpaceConstant.Switch.CLOSE) {
            result.setSeatState(SpaceConstant.Switch.CLOSE);
            result.setParentState(SpaceConstant.SpaceType.BUILD);
        } else {
            result.setSeatState(SpaceConstant.Switch.OPEN);
            result.setParentState(SpaceConstant.SpaceType.NONE);
        }
        return result;
    }

    public ParentConfAndState getParentProp(OneBOneF floor) {
        ParentConfAndState result = new ParentConfAndState();

        // 获取父节点的配置
        if (StringUtils.isNotBlank(floor.getFloorConfId())) {
            result.setConfId(floor.getFloorConfId());
            result.setParentConf(SpaceConstant.SpaceType.FLOOR);
        } else if (StringUtils.isNotBlank(floor.getBuildConfId())) {
            result.setConfId(floor.getBuildConfId());
            result.setParentConf(SpaceConstant.SpaceType.BUILD);
        } else {
            result.setConfId(null);
            result.setParentConf(SpaceConstant.SpaceType.NONE);
        }

        // 获取父节点的禁用状态
        if (floor.getBuildState() == SpaceConstant.Switch.CLOSE) {
            result.setSeatState(SpaceConstant.Switch.CLOSE);
            result.setParentState(SpaceConstant.SpaceType.BUILD);
        } else {
            result.setSeatState(SpaceConstant.Switch.OPEN);
            result.setParentState(SpaceConstant.SpaceType.NONE);
        }
        return result;
    }

    /** 当前用户能够管理的部门id集合 */
    public Set<String> getOrgIdList() {
        Set<String> orgIdList = SecurityUtils.getDataScope();
        if (CollectionUtils.isEmpty(orgIdList)) {
            return new HashSet<String>();
        }
        return orgIdList;
    }


    public double getDistanceMeter(GlobalCoordinates gpsFrom, GlobalCoordinates gpsTo, Ellipsoid ellipsoid) {
        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(ellipsoid, gpsFrom, gpsTo);
        return geoCurve.getEllipsoidalDistance();
    }


    public List<String> filterBuildIdByTag(Integer closeWindow, Integer haveSocket, Integer awayToilet, Integer awayDoor) {
        // 未勾选任何筛选条件，返回null
        if (closeWindow.intValue() == SpaceConstant.Switch.NO && //
                haveSocket.intValue() == SpaceConstant.Switch.NO && //
                awayToilet.intValue() == SpaceConstant.Switch.NO && //
                awayDoor.intValue() == SpaceConstant.Switch.NO) {
            return null;
        }

        LambdaQueryChainWrapper<SpaceTag> lambdaQuery = tagService.lambdaQuery();
        lambdaQuery.select(SpaceTag::getBuildId);
        if (closeWindow.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getWindow, closeWindow);
        }
        if (haveSocket.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getSocket, haveSocket);
        }
        if (awayToilet.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getToilet, awayToilet);
        }
        if (awayDoor.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getDoor, awayDoor);
        }
        lambdaQuery.groupBy(SpaceTag::getBuildId);

        // 未筛选出符合条件的，返回空集合
        List<SpaceTag> list = lambdaQuery.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<String>();
        }

        return list.stream().map(m -> m.getBuildId()).collect(Collectors.toList());
    }

    public List<String> filterRoomIdByTag(String buildId, Integer closeWindow, Integer haveSocket, Integer awayToilet, Integer awayDoor) {
        // 未勾选任何筛选条件，返回null
        if (closeWindow.intValue() == SpaceConstant.Switch.NO && //
                haveSocket.intValue() == SpaceConstant.Switch.NO && //
                awayToilet.intValue() == SpaceConstant.Switch.NO && //
                awayDoor.intValue() == SpaceConstant.Switch.NO) {
            return null;
        }

        LambdaQueryChainWrapper<SpaceTag> lambdaQuery = tagService.lambdaQuery();
        lambdaQuery.select(SpaceTag::getRoomId);
        lambdaQuery.eq(SpaceTag::getBuildId, buildId);
        if (closeWindow.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getWindow, closeWindow);
        }
        if (haveSocket.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getSocket, haveSocket);
        }
        if (awayToilet.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getToilet, awayToilet);
        }
        if (awayDoor.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getDoor, awayDoor);
        }
        lambdaQuery.groupBy(SpaceTag::getRoomId);

        // 未筛选出符合条件的，返回空集合
        List<SpaceTag> list = lambdaQuery.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<String>();
        }

        return list.stream().map(m -> m.getRoomId()).collect(Collectors.toList());
    }

    public List<String> filterSeatIdByTag(String roomId, Integer closeWindow, Integer haveSocket, Integer awayToilet, Integer awayDoor) {
        // 未勾选任何筛选条件，返回null
        if (closeWindow.intValue() == SpaceConstant.Switch.NO && //
                haveSocket.intValue() == SpaceConstant.Switch.NO && //
                awayToilet.intValue() == SpaceConstant.Switch.NO && //
                awayDoor.intValue() == SpaceConstant.Switch.NO) {
            return null;
        }

        LambdaQueryChainWrapper<SpaceTag> lambdaQuery = tagService.lambdaQuery();
        lambdaQuery.select(SpaceTag::getSeatId);
        lambdaQuery.eq(SpaceTag::getRoomId, roomId);
        if (closeWindow.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getWindow, closeWindow);
        }
        if (haveSocket.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getSocket, haveSocket);
        }
        if (awayToilet.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getToilet, awayToilet);
        }
        if (awayDoor.intValue() == SpaceConstant.Switch.YES) {
            lambdaQuery.eq(SpaceTag::getDoor, awayDoor);
        }

        // 未筛选出符合条件的，返回空集合
        List<SpaceTag> list = lambdaQuery.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<String>();
        }

        return list.stream().map(m -> m.getSeatId()).collect(Collectors.toList());
    }

}
