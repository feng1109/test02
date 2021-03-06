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

    // ????????????id?????????id??????????????????id
    public String showRoom(SpaceConfVO conf, Integer orderType) {

        List<String> orgIds = getDeptIdList();
        String userType = getUserType();
        String userId = getUserId();

        /************** ?????????????????????????????? **************/
        Integer conf_orderType = conf.getOrderType();
        if (conf_orderType == null || conf_orderType.intValue() != orderType.intValue()) {
            return "????????????????????????????????????";
        }


        /************** ???????????????????????????id?????????????????????????????????????????????????????????????????????null **************/
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


        /************** ????????????id???????????? **************/
        List<SpaceConfDeptVO> deptList = conf.getDeptList();
        boolean conform = false; // ?????????
        if (CollectionUtils.isEmpty(deptList)) {
            conform = true; // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        } else {
            // ??????????????????????????????????????????????????????
            for (SpaceConfDeptVO spaceConfDept : deptList) {
                if (spaceConfDept == null) {
                    continue;
                }
                if (orgIds.contains(spaceConfDept.getDeptId())) {
                    conform = true; // ??????
                    break;
                }
            }
        }
        if (!conform) {
            return "????????????????????????????????????";
        }

        /************** ????????????id???dutyList?????????????????????????????????????????? **************/
        List<SpaceConfDutyVO> dutyList = conf.getDutyList();
        conform = false; // ?????????
        if (CollectionUtils.isEmpty(dutyList)) {
            conform = true; // ??????
        } else {
            for (SpaceConfDutyVO duty : dutyList) {
                if (duty == null) {
                    continue;
                }
                if (userType.equals(duty.getDutyId())) { // ??????????????????????????????
                    conform = true; // ??????
                    break;
                }
            }
        }
        if (!conform) {
            return "????????????????????????????????????";
        }

        // ?????????????????????????????????null
        return null;
    }


    /** ????????????????????????????????????????????????????????? */
    @SuppressWarnings("deprecation")
    public String judgeOpenTime(SpaceConfVO conf, String startTime, String endTime, Date startDate, Date endDate, Integer orderType) {

        /************** ???????????? **************/
        /** ?????????????????????????????????????????????????????????????????????????????????????????????????????? */
        Integer conf_orderType = conf.getOrderType();
        if (conf_orderType.intValue() != orderType.intValue()) {
            return "????????????????????????";
        }

        /************** ?????????????????????dateList???null?????????????????? **************/
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
                        msg = sdf.format(endDate) + "???????????????";
                    }
                } else {
                    msg = sdf.format(startDate) + "???????????????";
                }
            }
            if (msg != null) {
                return msg;
            }
        }

        /************** ????????????weekList???null?????????????????? **************/
        List<SpaceConfWeekVO> weekList = conf.getWeekList();
        if (!CollectionUtils.isEmpty(weekList)) {
            int today = startDate.getDay();
            final int toFilter = today == 0 ? 7 : today; // [1,2,3,4,5,6,0] ==> [1,2,3,4,5,6,7]
            List<SpaceConfWeekVO> collect = weekList.stream().filter(m -> m != null && (toFilter + "").equals(m.getWeekId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                return SpaceConstant.Week.MAP.get(toFilter) + "???????????????";
            }
        }


        /************** ?????????????????????timeList???null?????????????????? **************/
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
                        msg = endTime + "???????????????";
                    }
                } else {
                    msg = startTime + "???????????????";
                }
            }
            if (msg != null) {
                return msg;
            }
        }

        // ????????????????????????
        Integer advanceDay = conf.getSubAdvanceDay();
        if (advanceDay != null) {
            // ??????????????????
            LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
            // ???????????????????????????
            LocalDateTime plusDays = today.plusDays(advanceDay.intValue());
            Timestamp timestamp = Timestamp.valueOf(plusDays);

            if (advanceDay.intValue() == 0) {
                if (endDate.getTime() > timestamp.getTime()) {
                    return "?????????????????????";
                }
            } else {
                if (endDate.getTime() > timestamp.getTime()) {
                    return "????????????" + advanceDay.intValue() + "????????????";
                }
            }

        }

        return null;
    }


    /** ?????????????????????????????????????????????????????????????????????????????????ids?????????????????????????????????????????? */
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

    /** ?????????????????????????????????????????????????????????????????????????????????ids???????????????????????????????????????????????? */
    public List<String> getMeetingRoomInUsedList(Date startDate, Collection<String> roomIdList) {
        return ((SpaceSeatMapper) seatService.getBaseMapper()).getMeetingRoomInUsedList(startDate, roomIdList);
    }

    private String getUserId() {
        return SecurityUtils.getUser().getSysUserDTO().getId();
    }

    private String getUserType() {
        String userType = SecurityUtils.getUser().getSysUserDTO().getUserType();
        if (StringUtils.isBlank(userType)) {
            throw BusinessException.of("?????????????????????");
        }
        return userType;
    }

    private List<String> getDeptIdList() {
        List<OrgDTO> sysOrgs = SecurityUtils.getUser().getSysUserDTO().getSysOrgs();
        if (CollectionUtils.isEmpty(sysOrgs)) {
            throw BusinessException.of("??????????????????????????????");
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

        List<String> openList = new ArrayList<String>(); // ??????
        List<String> freeList = new ArrayList<String>(); // ?????????
        for (SpaceConfVO conf : allConfList) {

            int orderTypeToUse = orderType;

            // ?????????????????????5?????????
            if (orderTypeToUse == SpaceConstant.OrderType.SINGLE_GROUP) {

                // ???????????????????????????????????????????????????
                if (conf.getOrderType().intValue() != SpaceConstant.OrderType.SINGLE_ONCE) {
                    continue;
                }

                // ??????????????????
                if (conf.getIsGroup().intValue() != SpaceConstant.Switch.YES) {
                    // ???????????????
                    continue;
                } else {
                    // ???????????????????????????
                    orderTypeToUse = SpaceConstant.OrderType.SINGLE_ONCE;
                }
            }
            // ?????????????????????1234???????????????????????????
            else {
                if (conf.getIsGroup().intValue() == SpaceConstant.Switch.YES) {
                    continue;
                }
            }

            // ??????????????????????????????????????????
            if (conf.getOrderType().intValue() != orderTypeToUse) {
                continue;
            }


            // ????????????????????????????????????????????????
            String showRoom = showRoom(conf, orderTypeToUse);
            if (showRoom != null) {
                continue;
            }
            openList.add(conf.getConfId());


            // ????????????????????????
            if (orderTypeToUse == SpaceConstant.OrderType.SINGLE_ONCE || orderTypeToUse == SpaceConstant.OrderType.MULTI_ONCE) {
                String judgeOpenTime = judgeOpenTime(conf, startTime, endTime, startDate, endDate, orderTypeToUse);
                if (judgeOpenTime == null) {
                    // ???????????????????????????????????????
                    freeList.add(conf.getConfId());
                }
            } else {
                // ?????????????????????????????????????????????????????????openList????????????
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

        // ????????????????????????
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

        // ??????????????????????????????
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

        // ????????????????????????
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

        // ??????????????????????????????
        if (floor.getBuildState() == SpaceConstant.Switch.CLOSE) {
            result.setSeatState(SpaceConstant.Switch.CLOSE);
            result.setParentState(SpaceConstant.SpaceType.BUILD);
        } else {
            result.setSeatState(SpaceConstant.Switch.OPEN);
            result.setParentState(SpaceConstant.SpaceType.NONE);
        }
        return result;
    }

    /** ?????????????????????????????????id?????? */
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
        // ????????????????????????????????????null
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

        // ?????????????????????????????????????????????
        List<SpaceTag> list = lambdaQuery.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<String>();
        }

        return list.stream().map(m -> m.getBuildId()).collect(Collectors.toList());
    }

    public List<String> filterRoomIdByTag(String buildId, Integer closeWindow, Integer haveSocket, Integer awayToilet, Integer awayDoor) {
        // ????????????????????????????????????null
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

        // ?????????????????????????????????????????????
        List<SpaceTag> list = lambdaQuery.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<String>();
        }

        return list.stream().map(m -> m.getRoomId()).collect(Collectors.toList());
    }

    public List<String> filterSeatIdByTag(String roomId, Integer closeWindow, Integer haveSocket, Integer awayToilet, Integer awayDoor) {
        // ????????????????????????????????????null
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

        // ?????????????????????????????????????????????
        List<SpaceTag> list = lambdaQuery.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<String>();
        }

        return list.stream().map(m -> m.getSeatId()).collect(Collectors.toList());
    }

}
